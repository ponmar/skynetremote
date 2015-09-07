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
import se.markstrom.skynet.skynetremote.apitask.AcceptEventsTask;
import se.markstrom.skynet.skynetremote.apitask.ApiThread;
import se.markstrom.skynet.skynetremote.apitask.ArmTask;
import se.markstrom.skynet.skynetremote.apitask.ConnectTask;
import se.markstrom.skynet.skynetremote.apitask.DisarmTask;
import se.markstrom.skynet.skynetremote.apitask.DisconnectTask;
import se.markstrom.skynet.skynetremote.apitask.GetCameraImageTask;
import se.markstrom.skynet.skynetremote.apitask.GetCamerasXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetControlXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetEventImageTask;
import se.markstrom.skynet.skynetremote.apitask.GetEventsXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetLogXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetSummaryXmlTask;
import se.markstrom.skynet.skynetremote.apitask.TemporaryDisarmTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOffAllDevicesTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnAllDevicesTask;
import se.markstrom.skynet.skynetremote.data.Event;
import se.markstrom.skynet.skynetremote.data.Device;
import se.markstrom.skynet.skynetremote.data.Summary;
import se.markstrom.skynet.skynetremote.xmlparser.CamerasXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.ControlXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.LogXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SettingsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparser.SummaryXmlParser;
import se.markstrom.skynet.skynetremote.xmlwriter.SettingsXmlWriter;

public class ApplicationWindow implements GUI {
	
	private static final String NAME = "Skynet Remote";
	
	private static final int EVENT_ID_COLUMN = 0;
	private static final int EVENT_TIME_COLUMN = 1;
	private static final int EVENT_SEVERITY_COLUMN = 2;
	private static final int EVENT_MESSAGE_COLUMN = 3;
	private static final int EVENT_SENSOR_COLUMN = 4;
	private static final int EVENT_ARMED_COLUMN = 5;
	private static final int EVENT_IMAGES_COLUMN = 6;

	private static final int CONTROL_NAME_COLUMN = 0;
	private static final int CONTROL_STATE_COLUMN = 1;
	private static final int CONTROL_TIMELEFT_COLUMN = 2;
	private static final int CONTROL_TYPE_COLUMN = 3;
	
	private Settings settings;
	
	private FileCache fileCache = new FileCache();
	
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
	private MenuItem actionGetControlItem;
	private MenuItem actionGetEventsItem;
	private MenuItem actionAcceptEventsItem;
	private MenuItem actionGetEventImagesItem;
	private MenuItem helpAboutItem;
	private Menu actionTemporaryDisarmMenu;
	private Menu cameraSnapshotMenu;
	
	private Image noneImage;
	private Image infoImage;
	private Image minorImage;
	private Image majorImage;
	
	private TrayItem trayItem;
	private Table eventsTable;
	private Table controlTable;
	private Text logText;
	
	private long prevPollEventId = -1;
	private String prevPollControlChecksum = "";
	private double prevPollLogTimestamp = -1;
	
	private long latestFetchedEventId = -1;
	
	private ApiThread apiThread = new ApiThread(this);

	private boolean apiWorking = false;
	private CONNECTED_STATE connectedState = CONNECTED_STATE.DISCONNECTED;
	private Summary summary = null;
	
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
		System.out.println("Settings written to file");
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
		
		Tray tray = display.getSystemTray();
		if (tray != null) {
			// Note: tray tool tip text is set in setTitle()
			trayItem = new TrayItem(tray, SWT.NONE);
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
		
		actionAcceptEventsItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionAcceptEventsItem.setText("Accept events");
		
		actionGetEventImagesItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionGetEventImagesItem.setText("Get event images");
		actionGetEventImagesItem.addSelectionListener(new ActionGetEventImagesItemListener());
		
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

		actionGetEventsItem = new MenuItem(actionMenu, SWT.PUSH);
		actionGetEventsItem.setText("Update events");
		actionGetEventsItem.addSelectionListener(new ActionGetEventsItemListener());
		
		actionGetControlItem = new MenuItem(actionMenu, SWT.PUSH);
		actionGetControlItem.setText("Update control");
		actionGetControlItem.addSelectionListener(new ActionGetControlItemListener());

		actionGetLogItem = new MenuItem(actionMenu, SWT.PUSH);
		actionGetLogItem.setText("Update log");
		actionGetLogItem.addSelectionListener(new ActionGetLogItemListener());
		
		// Action -> Accept events
		Menu actionAcceptEventsMenu = new Menu(shell, SWT.DROP_DOWN);
		actionAcceptEventsItem.setMenu(actionAcceptEventsMenu);
		
		MenuItem actionAcceptMinorEventsItem = new MenuItem(actionAcceptEventsMenu, SWT.PUSH);
		actionAcceptMinorEventsItem.setText("All with minor severity");
		actionAcceptMinorEventsItem.addSelectionListener(new ActionAcceptMinorEventsItemListener());

		MenuItem actionAcceptMajorEventsItem = new MenuItem(actionAcceptEventsMenu, SWT.PUSH);
		actionAcceptMajorEventsItem.setText("All with major severity");
		actionAcceptMajorEventsItem.addSelectionListener(new ActionAcceptMajorEventsItemListener());

		MenuItem actionAcceptAllEventsItem = new MenuItem(actionAcceptEventsMenu, SWT.PUSH);
		actionAcceptAllEventsItem.setText("All");
		actionAcceptAllEventsItem.addSelectionListener(new ActionAcceptAllEventsItemListener());
		
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
		
		// Tab: events
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

	    // Tab: control
	    controlTable = new Table(tf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
	    controlTable.setHeaderVisible(true);
	    controlTable.setLinesVisible(true);

	    TabItem controlTab = new TabItem(tf, SWT.BORDER);
	    controlTab.setText("Control");
	    TableColumn nameColumn = new TableColumn(controlTable, SWT.NULL);
	    nameColumn.setText("Device name");
	    nameColumn.pack();
	    TableColumn stateColumn = new TableColumn(controlTable, SWT.NULL);
	    stateColumn.setText("State");
	    stateColumn.pack();
	    TableColumn timeLeftColumn = new TableColumn(controlTable, SWT.NULL);
	    timeLeftColumn.setText("Time left");
	    timeLeftColumn.pack();
	    TableColumn typeColumn = new TableColumn(controlTable, SWT.NULL);
	    typeColumn.setText("Type");
	    typeColumn.pack();
	    
	    controlTab.setControl(controlTable);

	    // Tab: log
	    TabItem logTab = new TabItem(tf, SWT.BORDER);
	    logTab.setText("Log");
	    
	    logText = new Text(tf, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    logText.setEditable(false);
	    logTab.setControl(logText);	    
	    
	    shell.setMenuBar(menuBar);
		shell.open();
		
		updateGui();
	}
	
	private void updateGui() {
		
		boolean connected = connectedState == CONNECTED_STATE.CONNECTED;
		
		fileConnectItem.setEnabled(!connected);
		fileDisconnectItem.setEnabled(connected);
		fileSettingsItem.setEnabled(!connected);
		
		// Note: the armed state is unknown until summary XML/JSON has been fetched
		actionArmItem.setEnabled(connected);
		actionDisarmItem.setEnabled(connected);
		actionGetLogItem.setEnabled(connected);
		actionGetControlItem.setEnabled(connected);
		actionGetEventsItem.setEnabled(connected && !settings.getNewEvents);
		actionTurnOnAllDevicesItem.setEnabled(connected);
		actionTurnOffAllDevicesItem.setEnabled(connected);
		actionTemporaryDisarmItem.setEnabled(connected);
		actionAcceptEventsItem.setEnabled(connected);
		actionCameraSnapshotItem.setEnabled(connected);
		actionGetEventImagesItem.setEnabled(connected);

		if (trayItem != null) {
			trayItem.setImage(noneImage);
		}
		shell.setImage(noneImage);
		
		updateTitle();
	}
	
	private void updateTitle() {
		String connectedStr;
		switch (connectedState) {
		case DISCONNECTING:
			connectedStr = "disconnecting...";
			break;
		case DISCONNECTED:
			connectedStr = "disconnected";
			break;
		case CONNECTING:
			connectedStr = "connecting...";
			break;
		case CONNECTED:
			connectedStr = "connected";
			break;
		default:
			connectedStr = "unknown";
			break;
		}
		
		if (summary != null) {
			String apiWorkingStr;
			if (apiWorking) {
				apiWorkingStr = "working";
			}
			else {
				apiWorkingStr = "idle";
			}
			setTitle(NAME + " (" + connectedStr + ", " + summary.site + " is " + summary.getArmedStr() + ", API-thread is " + apiWorkingStr + ")");
		}
		else {
			setTitle(NAME + " (" + connectedStr + ")");
		}
	}
	
	private void setTitle(String title) {
		shell.setText(title);
		if (trayItem != null) {
			trayItem.setToolTipText(title);
		}
	}
	
	private void updateArmMenuItems() {
		actionArmItem.setEnabled(!summary.armed);
		actionDisarmItem.setEnabled(summary.armed);
		actionTemporaryDisarmItem.setEnabled(summary.armed);
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
			// TODO: don't write settings to file if window closed with 'x'/escape.
			SettingsWindow settingsWindow = new SettingsWindow(settings, shell);
			settingsWindow.run();
			settings = settingsWindow.getSettings();
			writeSettings();
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

	private class ActionGetControlItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			updateControl();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionGetEventsItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			updateEvents();
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
	
	private class ActionAcceptMinorEventsItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			acceptEvents(AcceptEventsTask.EventGroup.ALL_MINOR);
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class ActionAcceptMajorEventsItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			acceptEvents(AcceptEventsTask.EventGroup.ALL_MAJOR);
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class ActionAcceptAllEventsItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			acceptEvents(AcceptEventsTask.EventGroup.ALL);
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class ActionGetEventImagesItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			getEventImages();
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
			new AboutWindow(shell).run();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class EventSelectedListener implements MouseListener {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			getEventImages();
		}

		@Override
		public void mouseDown(MouseEvent e) {
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}
	}

	private void connect() {
		ConnectWindow connectWindow = new ConnectWindow(settings.host, settings.port, settings.protocol, shell);
		connectWindow.run();
		
		if (connectWindow.hasValidInput()) {
			String host = connectWindow.getHost();
			int port = connectWindow.getPort();
			SkynetAPI.Protocol protocol = connectWindow.getProtocol();
			String password = connectWindow.getPassword();
			boolean debug = false;

			settings.host = host;
			settings.port = port;
			settings.protocol = protocol;
			
			// Settings are written when a connection attempt has been done
			writeSettings();
			
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

	private void updateEvents() {
		if (!settings.getNewEvents) {
			apiThread.runTask(new GetEventsXmlTask());
		}
	}

	private void updateControl() {
		apiThread.runTask(new GetControlXmlTask());
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

	private void acceptEvents(AcceptEventsTask.EventGroup eventGroup) {
		apiThread.runTask(new AcceptEventsTask(eventGroup));
	}
	
	private void getEventImages() {
		for (TableItem selection : eventsTable.getSelection()) {
			System.out.println("Selected event id: " + selection.getText(EVENT_ID_COLUMN));
			int numImages = Integer.parseInt(selection.getText(EVENT_IMAGES_COLUMN));
			for (int imageIndex = 0; imageIndex < numImages; imageIndex++) {
				long eventId = Long.parseLong(selection.getText(EVENT_ID_COLUMN));
				byte [] jpegData = fileCache.getFileContent(Settings.createFilenameForEventImage(eventId, imageIndex)); 
				if (jpegData != null) {
					System.out.println("Found cached image!");
					updateEventImage(eventId, imageIndex, jpegData);					
				}
				else {
					apiThread.runTask(new GetEventImageTask(eventId, imageIndex));
				}
			}
		}
	}
	
	@Override
	public void updateConnectedState(CONNECTED_STATE state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				connectedState = state;
				updateGui();
				
				switch (state) {
				case CONNECTED:
					// Update cameras once to enable menu items
					apiThread.runTask(new GetCamerasXmlTask());
					
					getSummaryXmlRunnable.run();
					
				default:
					summary = null;
					break;
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
	public void updateControlXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Received control.xml");
				
				ControlXmlParser parser = new ControlXmlParser(xml);
				if (parser.isValid()) {
					controlTable.setRedraw(false);
					controlTable.removeAll();
					for (Device device : parser.getDevices()) {
						TableItem item = new TableItem(controlTable, SWT.NULL);
						item.setText(CONTROL_NAME_COLUMN, device.name);
						item.setText(CONTROL_STATE_COLUMN, device.getStateStr());
						item.setText(CONTROL_TIMELEFT_COLUMN, String.valueOf(device.timeLeft));
						item.setText(CONTROL_TYPE_COLUMN, device.getTypeStr());
					}
					for (int i=0; i<controlTable.getColumnCount(); i++) {
						controlTable.getColumn(i).pack();
					}
					controlTable.setRedraw(true);
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
							if (trayItem != null) {
								trayItem.setImage(infoImage);
							}
							shell.setImage(infoImage);
							break;
						case Event.MINOR:
							if (trayItem != null) {
								trayItem.setImage(minorImage);
							}
							shell.setImage(minorImage);
							break;
						case Event.MAJOR:
							if (trayItem != null) {
								trayItem.setImage(majorImage);
							}
							shell.setImage(majorImage);
							break;
						}
						
						if (settings.notifyOnNewEvent) {
							if (newMajorEvent) {
								new Notification(NAME, "New event with major severity detected!");
							}
							else if (newMinorEvent) {
								new Notification(NAME, "New event with minor severity detected!");
							}
							else if (newInfoEvent) {
								new Notification(NAME, "New event with info severity detected!");
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
				System.out.println("Received summary.xml");
				
				SummaryXmlParser parser = new SummaryXmlParser(xml);
				if (parser.isValid()) {
					summary = parser.getSummary();

					updateTitle();
					updateArmMenuItems();
					
					if (prevPollEventId != summary.latestEventId) {
						prevPollEventId = summary.latestEventId;
						
						if (settings.getNewEvents) {
							System.out.println("New event detected, requesting events");
							apiThread.runTask(new GetEventsXmlTask());
						}
					}
					
					if (!prevPollControlChecksum.equals(summary.controlChecksum)) {
						prevPollControlChecksum = summary.controlChecksum;
						
						if (settings.getNewControl) {
							System.out.println("New control checksum detected, requesting control");
							apiThread.runTask(new GetControlXmlTask());
						}
					}
					
					if (prevPollLogTimestamp != summary.logTimestamp) {
						prevPollLogTimestamp = summary.logTimestamp;
						
						if (settings.getNewLog) {
							System.out.println("New log timestamp detected, requesting log");
							apiThread.runTask(new GetLogXmlTask());
						}
					}
				}

				if (settings.pollSummary) {
					display.timerExec(settings.summaryPollInterval*1000, getSummaryXmlRunnable);
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
				fileCache.addFile(Settings.createFilenameForEventImage(eventId, imageIndex), jpegData);
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
				apiWorking = isWorking;
				updateTitle();
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
}
