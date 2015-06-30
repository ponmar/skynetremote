package se.markstrom.skynet.skynetremote;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.skynetremote.apitask.ApiThread;
import se.markstrom.skynet.skynetremote.apitask.ArmTask;
import se.markstrom.skynet.skynetremote.apitask.ConnectTask;
import se.markstrom.skynet.skynetremote.apitask.DisarmTask;
import se.markstrom.skynet.skynetremote.apitask.DisconnectTask;
import se.markstrom.skynet.skynetremote.apitask.GetCameraImageTask;
import se.markstrom.skynet.skynetremote.apitask.GetCamerasXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetEventImageTask;
import se.markstrom.skynet.skynetremote.apitask.GetEventsXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetLogXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetSummaryXmlTask;
import se.markstrom.skynet.skynetremote.apitask.TemporaryDisarmTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOffAllDevicesTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnAllDevicesTask;
import se.markstrom.skynet.skynetremote.data.Event;
import se.markstrom.skynet.skynetremote.data.Summary;
import se.markstrom.skynet.skynetremote.xmlparser.CamerasXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.LogXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SettingsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SummaryXmlParser;
import se.markstrom.skynet.skynetremote.xmlwriter.SettingsXmlWriter;

public class ApplicationWindow implements GUI {
	
	private static final String TITLE = "Skynet Remote";
	
	private static final int EVENT_ID_COLUMN = 0;
	private static final int EVENT_TIME_COLUMN = 1;
	private static final int EVENT_SEVERITY_COLUMN = 2;
	private static final int EVENT_MESSAGE_COLUMN = 3;
	private static final int EVENT_SENSOR_COLUMN = 4;
	private static final int EVENT_ARMED_COLUMN = 5;
	private static final int EVENT_IMAGES_COLUMN = 6;
	
	private Settings settings;
	
	private Display display;
	private Shell shell;
	
	private MenuItem fileConnectItem;
	private MenuItem fileDisconnectItem;
	private MenuItem fileSettingsItem;
	private MenuItem actionArmItem;
	private MenuItem actionDisarmItem;
	private MenuItem actionTemporaryDisarmItem;
	private MenuItem actionCameraSnapshotItem;
	private MenuItem actionTurnOnAllDevicesItem;
	private MenuItem actionTurnOffAllDevicesItem;
	private MenuItem actionGetLogItem;
	private MenuItem helpAboutItem;
	private Menu actionTemporaryDisarmMenu;
	private Menu cameraSnapshotMenu;
	
	private Image noneImage;
	private Image infoImage;
	private Image minorImage;
	private Image majorImage;
	
	private TrayItem trayItem;
	private Table eventsTable;
	private Text logText;
	
	private long prevPollEventId = -1;
	private long latestFetchedEventId = -1;
	
	private ApiThread apiThread = new ApiThread(this);
	
	private Runnable getSummaryXmlRunnable = new Runnable() {
		public void run() {
			apiThread.runTask(new GetSummaryXmlTask());
		}
	};
	
	public ApplicationWindow() {
		readSettings();
		createGui();
		apiThread.start();
	}
	
	public void close() {
		apiThread.close();
		
		// Needed to get rid of the tray icon without hovering with the mouse cursor
		trayItem.dispose();
		
		writeSettings();
	}

	private void readSettings() {
		SettingsXmlParser parser = new SettingsXmlParser();
		if (parser.isValid()) {
			System.out.println("Using settings from file");
			settings = parser.getSettings();
		}
		else {
			System.out.println("Using default settings");
			settings = new Settings();
		}
	}
	
	private void writeSettings() {
		SettingsXmlWriter settingsWriter = new SettingsXmlWriter(settings);
		settingsWriter.write();
	}
	
	private Image createImage(int width, int height, int color) {
		Image image = new Image(display, width, height);
		GC gc = new GC(image);
		gc.setBackground(display.getSystemColor(color));
		gc.fillRectangle(image.getBounds());
		gc.dispose();
		return image;
	}
	
	private void createGui() {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setSize(1024, 768);
		
		noneImage = createImage(16, 16, SWT.COLOR_BLACK);
		infoImage = createImage(16, 16, SWT.COLOR_GREEN);
		minorImage = createImage(16, 16, SWT.COLOR_YELLOW);
		majorImage = createImage(16, 16, SWT.COLOR_RED);
		
		shell.setImage(noneImage);
		
		Tray tray = display.getSystemTray();
		if (tray != null) {
			// Note: tray tool tip text is set in setTitle()
			trayItem = new TrayItem(tray, SWT.NONE);
			trayItem.setImage(noneImage);
		}
		else {
			System.out.println("No system tray available");
		}
		
		Menu menuBar = new Menu(shell, SWT.BAR);
		
		// File menu
		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");
		
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
		
		fileConnectItem = new MenuItem(fileMenu, SWT.PUSH);
		fileConnectItem.setText("&Connect...");
		fileConnectItem.addSelectionListener(new FileConnectItemListener());

		fileDisconnectItem = new MenuItem(fileMenu, SWT.PUSH);
		fileDisconnectItem.setText("&Disconnect");
		fileDisconnectItem.addSelectionListener(new FileDisconnectItemListener());

		fileSettingsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSettingsItem.setText("&Settings...");
		fileSettingsItem.addSelectionListener(new FileSettingsItemListener());
		
		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");
		fileExitItem.addSelectionListener(new FileExitItemListener());

		// Action menu
		MenuItem actionMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		actionMenuHeader.setText("&Action");
		
		Menu actionMenu = new Menu(shell, SWT.DROP_DOWN);
		actionMenuHeader.setMenu(actionMenu);

		actionArmItem = new MenuItem(actionMenu, SWT.PUSH);
		actionArmItem.setText("Arm");
		actionArmItem.addSelectionListener(new ActionArmItemListener());

		actionDisarmItem = new MenuItem(actionMenu, SWT.PUSH);
		actionDisarmItem.setText("Disarm");
		actionDisarmItem.addSelectionListener(new ActionDisarmItemListener());

		actionTemporaryDisarmItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionTemporaryDisarmItem.setText("Temporary disarm");
		
		actionCameraSnapshotItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionCameraSnapshotItem.setText("Camera snapshots");
		
		// Action -> Stream images from camera menu
		cameraSnapshotMenu = new Menu(shell, SWT.DROP_DOWN);
		actionCameraSnapshotItem.setMenu(cameraSnapshotMenu);
		
		// Action -> Temporary disarm
		actionTemporaryDisarmMenu = new Menu(shell, SWT.DROP_DOWN);
		actionTemporaryDisarmItem.setMenu(actionTemporaryDisarmMenu);

		MenuItem firstTempDisarm = new MenuItem(actionTemporaryDisarmMenu, SWT.PUSH);
		firstTempDisarm.setText("1 minute");
		firstTempDisarm.addSelectionListener(new ActionTemporaryDisarmItemListener(60));

		MenuItem secondTempDisarm = new MenuItem(actionTemporaryDisarmMenu, SWT.PUSH);
		secondTempDisarm.setText("5 minutes");
		secondTempDisarm.addSelectionListener(new ActionTemporaryDisarmItemListener(300));

		MenuItem thirdTempDisarm = new MenuItem(actionTemporaryDisarmMenu, SWT.PUSH);
		thirdTempDisarm.setText("10 minutes");
		thirdTempDisarm.addSelectionListener(new ActionTemporaryDisarmItemListener(600));

		actionTurnOnAllDevicesItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTurnOnAllDevicesItem.setText("Turn on all devices");
		actionTurnOnAllDevicesItem.addSelectionListener(new ActionTurnOnAllDevicesListener());

		actionTurnOffAllDevicesItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTurnOffAllDevicesItem.setText("Turn off all devices");
		actionTurnOffAllDevicesItem.addSelectionListener(new ActionTurnOffAllDevicesListener());

		actionGetLogItem = new MenuItem(actionMenu, SWT.PUSH);
		actionGetLogItem.setText("Update log");
		actionGetLogItem.addSelectionListener(new ActionGetLogItemListener());

		// Help menu
		MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("&Help");
		
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);
		
		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText("&About...");
		helpAboutItem.addSelectionListener(new HelpAboutItemListener());
		
		shell.setLayout(new FillLayout());

		// Tabs
		TabFolder tf = new TabFolder(shell, SWT.BORDER);
		
	    TabItem eventsTab = new TabItem(tf, SWT.BORDER);
	    eventsTab.setText("Events");
	    eventsTable = new Table(tf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
	    eventsTable.setHeaderVisible(true);
	    eventsTable.setLinesVisible(true);
	    eventsTable.addMouseListener(new EventSelectedListener());
	    TableColumn eventIdColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventIdColumn.setText("Id");
	    eventIdColumn.pack();
	    TableColumn eventTimeColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventTimeColumn.setText("Time");
	    eventTimeColumn.pack();
	    TableColumn eventSeverityColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventSeverityColumn.setText("Severity");
	    eventSeverityColumn.pack();
	    TableColumn eventMessageColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventMessageColumn.setText("Message");
	    eventMessageColumn.pack();
	    TableColumn eventSensorColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventSensorColumn.setText("Sensor");
	    eventSensorColumn.pack();
	    TableColumn eventArmedColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventArmedColumn.setText("Armed");
	    eventArmedColumn.pack();
	    TableColumn eventImagesColumn = new TableColumn(eventsTable, SWT.NULL);
	    eventImagesColumn.setText("Images");
	    eventImagesColumn.pack();
	    eventsTab.setControl(eventsTable);

	    TabItem controlTab = new TabItem(tf, SWT.BORDER);
	    controlTab.setText("Control");

	    TabItem logTab = new TabItem(tf, SWT.BORDER);
	    logTab.setText("Log");
	    
	    logText = new Text(tf, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    logText.setEditable(false);
	    logTab.setControl(logText);	    
	    
	    shell.setMenuBar(menuBar);
		shell.open();
		
		updateConnectedMenuItems(CONNECTED_STATE.DISCONNECTED);
	}
	
	private void updateConnectedMenuItems(CONNECTED_STATE connectedState) {
		
		boolean connected = connectedState == CONNECTED_STATE.CONNECTED;
		
		fileConnectItem.setEnabled(!connected);
		fileDisconnectItem.setEnabled(connected);
		
		// Note: the armed state is unknown until summary XML/JSON has been fetched
		actionArmItem.setEnabled(connected);
		actionDisarmItem.setEnabled(connected);
		actionGetLogItem.setEnabled(connected);
		actionTurnOnAllDevicesItem.setEnabled(connected);
		actionTurnOffAllDevicesItem.setEnabled(connected);
		actionTemporaryDisarmItem.setEnabled(connected);
		actionCameraSnapshotItem.setEnabled(connected);
		
		switch (connectedState) {
		case DISCONNECTING:
			setTitle(TITLE + " (disconnecting...)");
			break;
		case DISCONNECTED:
			setTitle(TITLE + " (disconnected)");
			break;
		case CONNECTING:
			setTitle(TITLE + " (connecting...)");
			break;
		case CONNECTED:
			setTitle(TITLE + " (connected)");
			break;
		}
	}
	
	private void setTitle(String title) {
		shell.setText(title);
		if (trayItem != null) {
			trayItem.setToolTipText(title);
		}
	}
	
	private void updateArmMenuItems(boolean armedState) {
		actionArmItem.setEnabled(!armedState);
		actionDisarmItem.setEnabled(armedState);
		actionTemporaryDisarmItem.setEnabled(armedState);
	}

	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private class FileExitItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			shell.close();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class FileConnectItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			connect();			
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class FileDisconnectItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			disconnect();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class FileSettingsItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			System.out.println("Settings");
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionArmItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			arm();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class ActionDisarmItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			disarm();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionTemporaryDisarmItemListener implements SelectionListener {
		private int seconds;
		
		public ActionTemporaryDisarmItemListener(int seconds) {
			this.seconds = seconds;
		}
		
		public void widgetSelected(SelectionEvent event) {
			temporaryDisarm(seconds);
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionGetLogItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			updateLog();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionTurnOnAllDevicesListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			turnOnAllDevices();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionTurnOffAllDevicesListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			turnOffAllDevices();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class ActionStreamItemListener implements SelectionListener {
		private int cameraIndex;
		
		public ActionStreamItemListener(int cameraIndex) {
			this.cameraIndex = cameraIndex;
		}
		
		public void widgetSelected(SelectionEvent event) {
			apiThread.runTask(new GetCameraImageTask(cameraIndex));
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class HelpAboutItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			System.out.println("Help!");;
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class EventSelectedListener implements MouseListener {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			Table table = (Table)e.getSource();

			// Note: currently only one selected event is supported 
			if (table.getSelectionCount() > 0) {
				TableItem selection = table.getSelection()[0];
				System.out.println("Selected event id: " + selection.getText(EVENT_ID_COLUMN));
				int numImages = Integer.parseInt(selection.getText(EVENT_IMAGES_COLUMN));
				for (int imageIndex = 0; imageIndex < numImages; imageIndex++) {
					long eventId = Long.parseLong(selection.getText(EVENT_ID_COLUMN));
					apiThread.runTask(new GetEventImageTask(eventId, imageIndex));
				}
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}
	}

	private void connect() {
		ConnectWindow connectWindow = new ConnectWindow(settings.host, settings.port);
		connectWindow.run();
		
		if (connectWindow.hasValidInput()) {
			String host = connectWindow.getHost();
			int port = connectWindow.getPort();
			SkynetAPI.Protocol protocol = connectWindow.getProtocol();
			String password = connectWindow.getPassword();
			boolean debug = false;

			settings.host = host;
			settings.port = port;
			
			apiThread.runTask(new ConnectTask(host, port, protocol, password, debug));
		}
	}

	private void disconnect() {
		apiThread.runTask(new DisconnectTask());
	}

	private void arm() {
		apiThread.runTask(new ArmTask());
	}
	
	private void disarm() {
		apiThread.runTask(new DisarmTask());
	}

	private void temporaryDisarm(int seconds) {
		apiThread.runTask(new TemporaryDisarmTask(seconds));
	}

	private void updateLog() {
		apiThread.runTask(new GetLogXmlTask());
	}
	
	private void turnOnAllDevices() {
		apiThread.runTask(new TurnOnAllDevicesTask());
	}

	private void turnOffAllDevices() {
		apiThread.runTask(new TurnOffAllDevicesTask());
	}

	@Override
	public void updateConnectedState(CONNECTED_STATE state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				updateConnectedMenuItems(state);
				switch (state) {
				case CONNECTED:
					apiThread.runTask(new GetCamerasXmlTask());
					
					if (settings.getNewEvents) {
						apiThread.runTask(new GetEventsXmlTask());
					}
					
					if (settings.pollSummary) {
						display.timerExec(settings.summaryPollInterval, getSummaryXmlRunnable);
					}
				default:
				}
			}
		});
	}
	
	@Override
	public void updateCamerasXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Received cameras.xml");
				
				CamerasXmlParser parser = new CamerasXmlParser(xml);
				if (parser.isValid()) {
					for (Integer cameraIndex : parser.getCameraIndexes()) {
						MenuItem cameraMenuItem = new MenuItem(cameraSnapshotMenu, SWT.PUSH);
						cameraMenuItem.setText("Camera " + (cameraIndex+1));
						cameraMenuItem.addSelectionListener(new ActionStreamItemListener(cameraIndex));
					}
				}
			}
		});
	}
	
	@Override
	public void updateEventsXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("New events.xml");
				
				EventsXmlParser parser = new EventsXmlParser(xml);
				if (parser.isValid()) {
					System.out.println("Parsed events.xml");

					eventsTable.setRedraw(false);
					
					eventsTable.removeAll();
					List<Event> events = parser.getEvents();
					
					if (!events.isEmpty()) {
						int highestSeverity = 0;

						boolean newInfoEvent = false;
						boolean newMinorEvent = false;
						boolean newMajorEvent = false;
						
						ListIterator<Event> it = events.listIterator(events.size());
						while (it.hasPrevious()) {
							Event event = it.previous();
							
							if (event.id > latestFetchedEventId) {
								switch (event.severity) {
								case Event.INFO:
									newInfoEvent = true;
									break;
								case Event.MINOR:
									newMinorEvent = true;
									break;
								case Event.MAJOR:
									newMajorEvent = true;
									break;
								}
								
							}
							
							TableItem item = new TableItem(eventsTable, SWT.NULL);
							item.setText(EVENT_ID_COLUMN, String.valueOf(event.id));
							item.setText(EVENT_TIME_COLUMN, event.time);
							item.setText(EVENT_SEVERITY_COLUMN, event.getSeverityStr());
							item.setText(EVENT_MESSAGE_COLUMN, event.message);
							item.setText(EVENT_SENSOR_COLUMN, event.sensor);
							item.setText(EVENT_ARMED_COLUMN, event.getArmedStr());
							item.setText(EVENT_IMAGES_COLUMN, String.valueOf(event.images));
							
							if (event.severity > highestSeverity) {
								highestSeverity = event.severity;
							}
						}
						
						for (int i=0; i<eventsTable.getColumnCount(); i++) {
							eventsTable.getColumn(i).pack();
						}
						
						eventsTable.setRedraw(true);
						eventsTable.redraw();
						
						switch (highestSeverity) {
						case Event.INFO:
							trayItem.setImage(infoImage);
							shell.setImage(infoImage);
							break;
						case Event.MINOR:
							trayItem.setImage(minorImage);
							shell.setImage(minorImage);
							break;
						case Event.MAJOR:
							trayItem.setImage(majorImage);
							shell.setImage(majorImage);
							break;
						}
						
						if (settings.notifyOnNewEvent) {
							if (newMajorEvent) {
								new Notification(TITLE, "New event with major severity detected!");
							}
							else if (newMinorEvent) {
								new Notification(TITLE, "New event with minor severity detected!");
							}
							else if (newInfoEvent) {
								new Notification(TITLE, "New event with info severity detected!");
							}
						}
						
						latestFetchedEventId = events.get(events.size() - 1).id;
					}					
				}
			}
		});
	}
	
	@Override
	public void updateLogXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				LogXmlParser parser = new LogXmlParser(xml);
				if (parser.isValid()) {
					logText.setText(parser.getLogText());
				}
			}
		});
	}

	@Override
	public void updateSummaryXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				SummaryXmlParser parser = new SummaryXmlParser(xml);
				if (parser.isValid()) {
					Summary summary = parser.getSummary();
					
					updateArmMenuItems(summary.armed);
					
					if (prevPollEventId != summary.latestEventId) {
						prevPollEventId = summary.latestEventId;
						
						if (settings.getNewEvents) {
							System.out.println("New event detected, requesting events");
							apiThread.runTask(new GetEventsXmlTask());
						}
					}
				}

				if (settings.pollSummary) {
					display.timerExec(settings.summaryPollInterval, getSummaryXmlRunnable);
				}
			}
		});
	}

	@Override
	public void updateEventImage(long eventId, int imageIndex, byte[] jpegData) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				String windowTitle = "Event " + eventId + " (image " + (imageIndex+1) + ")"; 
				new ImageWindow(windowTitle, jpegData);
			}
		});
	}
	
	@Override
	public void updateCameraImage(int cameraIndex, byte[] jpegData) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				new ImageWindow("Camera " + (cameraIndex+1) + " snapshot", jpegData);
			}
		});
	}

	@Override
	public void updateApiWorkingState(boolean isWorking) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO: update gui
				System.out.println("Working: " + isWorking);
			}
		});
	}

	@Override
	public void showApiError(String message) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Skynet API Error");
				dialog.setMessage(message);
				dialog.open();
			}
		});
	}

	public static void main(String[] args) {
		ApplicationWindow window = new ApplicationWindow();
		window.run();
		window.close();
	}
}
