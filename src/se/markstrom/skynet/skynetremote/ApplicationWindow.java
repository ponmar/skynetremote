package se.markstrom.skynet.skynetremote;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.skynetremote.apitask.ApiThread;
import se.markstrom.skynet.skynetremote.apitask.ArmTask;
import se.markstrom.skynet.skynetremote.apitask.ConnectTask;
import se.markstrom.skynet.skynetremote.apitask.DisarmTask;
import se.markstrom.skynet.skynetremote.apitask.DisconnectTask;
import se.markstrom.skynet.skynetremote.apitask.GetEventImageTask;
import se.markstrom.skynet.skynetremote.apitask.GetEventsXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetLogXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetSummaryXmlTask;
import se.markstrom.skynet.skynetremote.apitask.TemporaryDisarmTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOffAllDevicesTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnAllDevicesTask;
import se.markstrom.skynet.skynetremote.xmlparsing.Event;
import se.markstrom.skynet.skynetremote.xmlparsing.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparsing.LogXmlParser;
import se.markstrom.skynet.skynetremote.xmlparsing.SummaryXmlParser;

public class ApplicationWindow implements GUI {
	
	private static final String TITLE = "Skynet Remote";
	
	private static final int EVENT_ID_COLUMN = 0;
	private static final int EVENT_IMAGES_COLUMN = 6;
	
	private Settings settings = new Settings();
	
	private Display display;
	private Shell shell;
	
	private MenuItem fileConnectItem;
	private MenuItem fileDisconnectItem;
	private MenuItem actionArmItem;
	private MenuItem actionDisarmItem;
	private MenuItem actionTempDisarmItem;
	private MenuItem actionGetLogItem;
	private MenuItem actionTurnOnAllDevicesItem;
	private MenuItem actionTurnOffAllDevicesItem;
	
	private Table eventsTable;
	private Text logText;
	
	private long prevLatestEventId = -1;
	
	private ApiThread apiThread = new ApiThread(this);
	
	private Runnable getSummaryXmlRunnable = new Runnable() {
		public void run() {
			apiThread.runTask(new GetSummaryXmlTask());
		}
	};
	
	public ApplicationWindow() {
		createGui();
		apiThread.start();
	}
	
	public void close() {
		apiThread.close();
	}

	private void createGui() {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setSize(1024, 768);
		
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

		actionTempDisarmItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTempDisarmItem.setText("Temporary disarm (5 min)");
		actionTempDisarmItem.addSelectionListener(new ActionTemporaryDisarmItemListener());

		actionGetLogItem = new MenuItem(actionMenu, SWT.PUSH);
		actionGetLogItem.setText("Update log");
		actionGetLogItem.addSelectionListener(new ActionGetLogItemListener());

		actionTurnOnAllDevicesItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTurnOnAllDevicesItem.setText("Turn on all devices");
		actionTurnOnAllDevicesItem.addSelectionListener(new ActionTurnOnAllDevicesListener());

		actionTurnOffAllDevicesItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTurnOffAllDevicesItem.setText("Turn on all devices");
		actionTurnOffAllDevicesItem.addSelectionListener(new ActionTurnOffAllDevicesListener());

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

	    TabItem streamingTab = new TabItem(tf, SWT.BORDER);
	    streamingTab.setText("Live Streaming");

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
		actionTempDisarmItem.setEnabled(connected);
		
		switch (connectedState) {
		case DISCONNECTING:
			shell.setText(TITLE + " (disconnecting...)");
			break;
		case DISCONNECTED:
			shell.setText(TITLE + " (disconnected)");
			break;
		case CONNECTING:
			shell.setText(TITLE + " (connecting...)");
			break;
		case CONNECTED:
			shell.setText(TITLE + " (connected)");
			break;
		}
	}
	
	private void updateArmMenuItems(boolean armedState) {
		actionArmItem.setEnabled(!armedState);
		actionDisarmItem.setEnabled(armedState);
		actionTempDisarmItem.setEnabled(armedState);
	}

	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
	
	private class FileExitItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			shell.close();
			display.dispose();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			shell.close();
			display.dispose();
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
		public void widgetSelected(SelectionEvent event) {
			temporaryDisarm(300);
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
		ConnectWindow connectWindow = new ConnectWindow();
		connectWindow.run();
		
		if (connectWindow.hasValidInput()) {
			String host = connectWindow.getHost();
			int port = connectWindow.getPort();
			SkynetAPI.Protocol protocol = connectWindow.getProtocol();
			String password = connectWindow.getPassword();
			boolean debug = false;

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
						prevLatestEventId = events.get(events.size() - 1).id;

						ListIterator<Event> it = events.listIterator(events.size());
						while (it.hasPrevious()) {
							Event event = it.previous();
							TableItem item = new TableItem(eventsTable, SWT.NULL);
							item.setText(EVENT_ID_COLUMN, String.valueOf(event.id));
							item.setText(1, event.time);
							item.setText(2, event.getSeverityStr());
							item.setText(3, event.message);
							item.setText(4, event.sensor);
							item.setText(5, event.getArmedStr());
							item.setText(EVENT_IMAGES_COLUMN, String.valueOf(event.images));
						}
						
						for (int i=0; i<eventsTable.getColumnCount(); i++) {
							eventsTable.getColumn(i).pack();
						}
						
						eventsTable.setRedraw(true);
						eventsTable.redraw();
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
					
					updateArmMenuItems(parser.getArmedState());
					
					if (prevLatestEventId != parser.getLatestEventId()) {
						prevLatestEventId = parser.getLatestEventId();
						
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
				ImageWindow imageWindow = new ImageWindow(windowTitle, jpegData);
				imageWindow.run();
			}
		});
	}
	
	@Override
	public void updateCameraImage(int cameraIndex, byte[] jpegData) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				// TODO: update gui
				System.out.println("Received new camera image");
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
