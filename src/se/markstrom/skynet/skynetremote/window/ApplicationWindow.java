package se.markstrom.skynet.skynetremote.window;

import java.io.File;
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
import org.eclipse.swt.widgets.DirectoryDialog;
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
import se.markstrom.skynet.skynetremote.FileCache;
import se.markstrom.skynet.skynetremote.FileWriter;
import se.markstrom.skynet.skynetremote.GUI;
import se.markstrom.skynet.skynetremote.Settings;
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
import se.markstrom.skynet.skynetremote.apitask.TurnOffDeviceTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnAllDevicesTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnDeviceTask;
import se.markstrom.skynet.skynetremote.model.Camera;
import se.markstrom.skynet.skynetremote.model.Device;
import se.markstrom.skynet.skynetremote.model.Event;
import se.markstrom.skynet.skynetremote.model.Model;
import se.markstrom.skynet.skynetremote.model.Summary;

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
	private MenuItem actionTurnOnDevicesItem;
	private MenuItem actionTurnOffDevicesItem;
	private MenuItem actionGetLogItem;
	private MenuItem actionGetControlItem;
	private MenuItem actionGetEventsItem;
	private MenuItem actionAcceptEventsItem;
	private MenuItem actionGetEventImagesItem;
	private MenuItem actionSaveEventImagesItem;
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
	
	private ApiThread apiThread = new ApiThread(this);

	private boolean apiWorking = false;
	private CONNECTED_STATE connectedState = CONNECTED_STATE.DISCONNECTED;
	
	private String imagesDirectory = null;
	
	private Model model = new Model();
	
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
		
		// Needed to get rid of the tray icon without hovering with the mouse cursor
		trayItem.dispose();
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
		shell.setLayout(new FillLayout());
		
		createIcons();
		createTray();
		createMenuBar();
		createTabs();
	    
		shell.open();
		
		updateGui();
	}
	
	private void createIcons() {
		noneImage = createImage(16, 16, SWT.COLOR_BLACK);
		infoImage = createImage(16, 16, SWT.COLOR_GREEN);
		minorImage = createImage(16, 16, SWT.COLOR_YELLOW);
		majorImage = createImage(16, 16, SWT.COLOR_RED);
	}
	
	private void createTray() {
		Tray tray = display.getSystemTray();
		if (tray != null) {
			// Note: tray tool tip text is set in setTitle()
			trayItem = new TrayItem(tray, SWT.NONE);
		}
		else {
			System.out.println("No system tray available");
		}
	}
	
	private void createMenuBar() {
		Menu menuBar = new Menu(shell, SWT.BAR);

		createFileMenu(menuBar);
		createActionMenu(menuBar);
		createHelpMenu(menuBar);
		
		shell.setMenuBar(menuBar);
	}
	
	private void createFileMenu(Menu menuBar) {
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
		
		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileSettingsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSettingsItem.setText("&Settings...");
		fileSettingsItem.addSelectionListener(new FileSettingsItemListener());
		
		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");
		fileExitItem.addSelectionListener(new FileExitItemListener());
	}

	private void createActionMenu(Menu menuBar) {
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
		
		actionAcceptEventsItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionAcceptEventsItem.setText("Accept events");
		
		new MenuItem(actionMenu, SWT.SEPARATOR);
		
		actionCameraSnapshotItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionCameraSnapshotItem.setText("Camera snapshots");

		actionGetEventImagesItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionGetEventImagesItem.setText("Get event images");
		actionGetEventImagesItem.addSelectionListener(new ActionGetEventImagesItemListener());

		actionSaveEventImagesItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionSaveEventImagesItem.setText("Save event images...");
		actionSaveEventImagesItem.addSelectionListener(new ActionSaveEventImagesItemListener());

		new MenuItem(actionMenu, SWT.SEPARATOR);
		
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

		actionTurnOnDevicesItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTurnOnDevicesItem.setText("Turn on devices");
		actionTurnOnDevicesItem.addSelectionListener(new ActionTurnOnDevicesListener());
		
		actionTurnOffDevicesItem = new MenuItem(actionMenu, SWT.PUSH);
		actionTurnOffDevicesItem.setText("Turn off devices");
		actionTurnOffDevicesItem.addSelectionListener(new ActionTurnOffDevicesListener());

		new MenuItem(actionMenu, SWT.SEPARATOR);
		
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
	}
	
	private void createHelpMenu(Menu menuBar) {
		MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("&Help");
		
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);
		
		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText("&About...");
		helpAboutItem.addSelectionListener(new HelpAboutItemListener());
	}
	
	private void createTabs() {
		TabFolder tf = new TabFolder(shell, SWT.BORDER);
		createEventsTab(tf);
	    createControlTab(tf);
	    createLogTab(tf);
	}

	private void createEventsTab(TabFolder tf) {
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
	}

	private void createControlTab(TabFolder tf) {
	    controlTable = new Table(tf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
	    controlTable.setHeaderVisible(true);
	    controlTable.setLinesVisible(true);
	    controlTable.addMouseListener(new DeviceSelectedListener());

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
	}

	private void createLogTab(TabFolder tf) {
	    TabItem logTab = new TabItem(tf, SWT.BORDER);
	    logTab.setText("Log");
	    
	    logText = new Text(tf, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    logText.setEditable(false);
	    logTab.setControl(logText);
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
		actionGetEventsItem.setEnabled(connected && !model.getSettings().getNewEvents);
		actionTurnOnAllDevicesItem.setEnabled(connected);
		actionTurnOffAllDevicesItem.setEnabled(connected);
		actionTurnOnDevicesItem.setEnabled(connected);
		actionTurnOffDevicesItem.setEnabled(connected);
		actionTemporaryDisarmItem.setEnabled(connected);
		actionAcceptEventsItem.setEnabled(connected);
		actionCameraSnapshotItem.setEnabled(connected);
		actionGetEventImagesItem.setEnabled(connected);
		actionSaveEventImagesItem.setEnabled(connected);

		updateCameras();
		
		setIcon(noneImage);
		
		updateTitle();
	}
	
	private void setIcon(Image image) {
		if (trayItem != null) {
			trayItem.setImage(image);
		}
		shell.setImage(image);
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
		
		Summary summary = model.getSummary();
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
		Summary summary = model.getSummary();
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
			SettingsWindow settingsWindow = new SettingsWindow(model.getSettings(), shell);
			settingsWindow.run();
			Settings newSettings = settingsWindow.getSettings();
			if (newSettings != null) {
				model.updateSettings(newSettings);
			}
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

	private class ActionTurnOnDevicesListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			turnOnDevices();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionTurnOffDevicesListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			turnOffDevices();
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
			getEventImages(true, false);
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionSaveEventImagesItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			getEventImages(false, true);
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
			getEventImages(true, false);
		}

		@Override
		public void mouseDown(MouseEvent e) {
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}
	}

	private class DeviceSelectedListener implements MouseListener {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			toggleDevicesState();
		}

		@Override
		public void mouseDown(MouseEvent e) {
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}
	}
	
	private void connect() {
		ConnectWindow connectWindow = new ConnectWindow(model.getSettings().host, model.getSettings().port, model.getSettings().protocol, shell);
		connectWindow.run();
		
		if (connectWindow.hasValidInput()) {
			String host = connectWindow.getHost();
			int port = connectWindow.getPort();
			SkynetAPI.Protocol protocol = connectWindow.getProtocol();
			String password = connectWindow.getPassword();
			boolean debug = false;

			// Note that settings are written when a connection attempt has been done
			model.updateSettingsFromGui(host, port, protocol);
			
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
		if (!model.getSettings().getNewEvents) {
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
	
	private void getEventImages(boolean show, boolean save) {

		if (save) {
			DirectoryDialog dialog = new DirectoryDialog(shell);
			imagesDirectory = dialog.open();
		}

		for (TableItem selection : eventsTable.getSelection()) {
			long eventId = (long)selection.getData();
			Event event = model.getEvent(eventId);
			if (event != null) {
				for (int imageIndex = 0; imageIndex < event.images; imageIndex++) {
					byte [] jpegData = fileCache.getFileContent(Settings.createFilenameForEventImage(event.id, imageIndex)); 
					if (jpegData != null) {
						if (show) {
							updateEventImage(eventId, imageIndex, jpegData, show, save);
						}
						if (save && imagesDirectory != null) {
							String filename = imagesDirectory + File.separatorChar + Settings.createFilenameForEventImage(event.id, imageIndex);
							FileWriter.saveFile(filename, jpegData);
						}
					}
					else {
						apiThread.runTask(new GetEventImageTask(eventId, imageIndex, show, save));
					}
				}
			}
		}
	}
	
	private void turnOnDevices() {
		setDevicesState(true);
	}

	private void turnOffDevices() {
		setDevicesState(false);
	}

	private void setDevicesState(boolean state) {
		for (TableItem selection : controlTable.getSelection()) {
			int deviceId = (int)selection.getData();
			Device device = model.getDevice(deviceId);
			if (device != null && device.state != state) {
				if (state) {
					apiThread.runTask(new TurnOnDeviceTask(deviceId));
				}
				else {
					apiThread.runTask(new TurnOffDeviceTask(deviceId));
				}
			}
		}
	}
	
	private void toggleDevicesState() {
		for (TableItem selection : controlTable.getSelection()) {
			Device device = (Device)selection.getData();
			if (device.state) {
				apiThread.runTask(new TurnOffDeviceTask(device.id));
			}
			else {
				apiThread.runTask(new TurnOnDeviceTask(device.id));
			}
		}
	}
	
	@Override
	public void updateConnectedState(CONNECTED_STATE state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				connectedState = state;
				
				if (state == CONNECTED_STATE.CONNECTED) {
					// Reset old fetched data when connected, cause it might be invalid for this host.
					model.reset();
					
					// Update cameras once to enable menu items
					apiThread.runTask(new GetCamerasXmlTask());
					
					getSummaryXmlRunnable.run();
				}
				
				updateGui();
			}
		});
	}
	
	@Override
	public void updateCamerasXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Received cameras.xml");
				
				if (model.updateCameras(xml)) {
					updateCameras();
				}
			}
		});
	}
	
	private void updateCameras() {
		MenuItem[] menuItems = cameraSnapshotMenu.getItems();
		for (MenuItem item : menuItems) {
		    item.dispose();
		}
		
		for (Camera camera : model.getCameras()) {
			MenuItem cameraMenuItem = new MenuItem(cameraSnapshotMenu, SWT.PUSH);
			cameraMenuItem.setText("Camera " + (camera.index+1) + " [" + camera.width + "x" + camera.height + "]");
			cameraMenuItem.addSelectionListener(new ActionStreamItemListener(camera.index));
		}
	}
	
	@Override
	public void updateControlXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Received control.xml");
				if (model.updateDevices(xml)) {
					controlTable.setRedraw(false);
					controlTable.removeAll();
					for (Device device : model.getDevices()) {
						TableItem item = new TableItem(controlTable, SWT.NULL);
						item.setText(CONTROL_NAME_COLUMN, device.name);
						item.setText(CONTROL_STATE_COLUMN, device.getStateStr());
						item.setText(CONTROL_TIMELEFT_COLUMN, String.valueOf(device.timeLeft));
						item.setText(CONTROL_TYPE_COLUMN, device.getTypeStr());
						item.setData(device.id);
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
				long prevLatestEventId = model.getLatestEventId();
				if (model.updateEvents(xml)) {
					System.out.println("Parsed events.xml");

					eventsTable.setRedraw(false);
					
					eventsTable.removeAll();
					List<Event> events = model.getEvents();
					
					if (!events.isEmpty()) {
						int highestSeverity = 0;

						boolean newInfoEvent = false;
						boolean newMinorEvent = false;
						boolean newMajorEvent = false;
						
						ListIterator<Event> it = events.listIterator(events.size());
						while (it.hasPrevious()) {
							Event event = it.previous();
							
							if (event.id > prevLatestEventId) {
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
							item.setData(event.id);
							
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
							setIcon(infoImage);
							break;
						case Event.MINOR:
							setIcon(minorImage);
							break;
						case Event.MAJOR:
							setIcon(majorImage);
							break;
						}
						
						if (model.getSettings().notifyOnNewEvent) {
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
				if (model.updateLog(xml)) {
					logText.setText(model.getLog().text);
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
				
				long prevPollEventId = model.getLatestEventIdFromSummary();
				String prevPollControlChecksum = model.getControlChecksumFromSummary();
				double prevPollLogTimestamp = model.getLogTimestampFromSummary();
				
				if (model.updateSummary(xml)) {
					Summary summary = model.getSummary();

					updateTitle();
					updateArmMenuItems();
					
					if (prevPollEventId != summary.latestEventId) {
						if (model.getSettings().getNewEvents) {
							System.out.println("New event detected, requesting events");
							apiThread.runTask(new GetEventsXmlTask());
						}
					}
					
					if (!prevPollControlChecksum.equals(summary.controlChecksum)) {
						if (model.getSettings().getNewControl) {
							System.out.println("New control checksum detected, requesting control");
							apiThread.runTask(new GetControlXmlTask());
						}
					}
					
					if (prevPollLogTimestamp != summary.logTimestamp) {
						if (model.getSettings().getNewLog) {
							System.out.println("New log timestamp detected, requesting log");
							apiThread.runTask(new GetLogXmlTask());
						}
					}
				}

				if (model.getSettings().pollSummary) {
					display.timerExec(model.getSettings().summaryPollInterval*1000, getSummaryXmlRunnable);
				}
			}
		});
	}

	@Override
	public void updateEventImage(long eventId, int imageIndex, byte[] jpegData, boolean show, boolean save) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				
				fileCache.addFile(Settings.createFilenameForEventImage(eventId, imageIndex), jpegData);
				
				if (show) {
					String windowTitle = "Event " + eventId + " (image " + (imageIndex+1) + ")"; 
					new ImageWindow(windowTitle, jpegData);
				}
				
				if (save) {
					String filename = imagesDirectory + File.separatorChar + Settings.createFilenameForEventImage(eventId, imageIndex);
					FileWriter.saveFile(filename, jpegData);
				}
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
