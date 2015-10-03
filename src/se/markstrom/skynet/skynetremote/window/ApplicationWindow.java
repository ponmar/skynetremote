package se.markstrom.skynet.skynetremote.window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
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
import se.markstrom.skynet.skynetremote.apitask.GetSensorsXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetSummaryXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetWeatherXmlTask;
import se.markstrom.skynet.skynetremote.apitask.TemporaryDisarmTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOffAllDevicesTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOffDeviceTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnAllDevicesTask;
import se.markstrom.skynet.skynetremote.apitask.TurnOnDeviceTask;
import se.markstrom.skynet.skynetremote.apitask.GetCameraImageTask.ImageType;
import se.markstrom.skynet.skynetremote.logging.LogLevelFilter;
import se.markstrom.skynet.skynetremote.logging.SwtLogHandler;
import se.markstrom.skynet.skynetremote.model.Camera;
import se.markstrom.skynet.skynetremote.model.Device;
import se.markstrom.skynet.skynetremote.model.Event;
import se.markstrom.skynet.skynetremote.model.Event.Severity;
import se.markstrom.skynet.skynetremote.model.Model;
import se.markstrom.skynet.skynetremote.model.Sensor;
import se.markstrom.skynet.skynetremote.model.Settings;
import se.markstrom.skynet.skynetremote.model.WeatherReport;

public class ApplicationWindow implements GUI {

	private static final Logger log = Logger.getLogger(ApplicationWindow.class.getName());
	static {
		log.setLevel(Level.ALL);
	}
	
	private static final String NAME = "Skynet Remote";
	private static final String API_ERROR_NAME = "Skynet API Error";
	
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
	private MenuItem actionCameraStreamItem;
	private MenuItem actionTurnOnAllDevicesItem;
	private MenuItem actionTurnOffAllDevicesItem;
	private MenuItem actionTurnOnDevicesItem;
	private MenuItem actionTurnOffDevicesItem;
	private MenuItem actionGetDataItem;
	private MenuItem actionAcceptEventsItem;
	private MenuItem actionGetEventImagesItem;
	private MenuItem actionSaveEventImagesItem;
	private MenuItem helpAboutItem;
	private Menu actionTemporaryDisarmMenu;
	private Menu cameraSnapshotMenu;
	private Menu cameraStreamMenu;
	
	private Image noneIcon;
	private Image infoIcon;
	private Image minorIcon;
	private Image majorIcon;
	
	private Image deviceOffIcon;
	private Image deviceOnIcon;
	
	private TrayItem trayItem;
	private Table eventsTable;
	private Table controlTable;
	private Table weatherTable;
	private Table sensorsTable;
	private Text logText;
	private Text remoteLogText;
	
	private TabFolder tabFolder;
	
	private ApiThread apiThread = new ApiThread(this);

	private boolean apiWorking = false;
	private ConnectedState connectedState = ConnectedState.DISCONNECTED;
	
	private String imagesDirectory = null;
	
	private Model model = new Model();
	
	private ArrayList<CameraStreamWindow> streamWindows = new ArrayList<CameraStreamWindow>(); 
	
	private Runnable getSummaryXmlRunnable = new Runnable() {
		public void run() {
			apiThread.runTask(new GetSummaryXmlTask());
		}
	};
	
	public ApplicationWindow() {
		createGui();
		
		// The GUI widgets for logging are now created. Setup the custom log handler!
		// Add the new log handler to the package level above all logging classes to
		// intercept all log messages.
		Logger commonLogger = Logger.getLogger("se.markstrom.skynet.skynetremote");
		SwtLogHandler logHandler = new SwtLogHandler(display, logText);
		if (model.getSettings().logDetails) {
			logHandler.setFilter(new LogLevelFilter(Level.ALL));
		}
		else {
			logHandler.setFilter(new LogLevelFilter(Level.INFO));
		}
		commonLogger.addHandler(logHandler);
		
		apiThread.start();
		
		// Open connect window once when the application starts
		connect();
	}
	
	public void close() {
		apiThread.close();
		
		// Needed to get rid of the tray icon without hovering with the mouse cursor
		trayItem.dispose();
	}
	
	private void createGui() {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setSize(1024, 768);
		shell.setLayout(new FillLayout());
		
		createIcons(32);
		createTray();
		createMenuBar();
		createTabs();
		
		shell.open();
		
		updateGui();
		addKeyBindings();
	}
	
	private void addKeyBindings() {
		// 1-9 is used for selecting tabs
		display.addFilter(SWT.KeyDown, new Listener() {
			@Override
		    public void handleEvent(org.eclipse.swt.widgets.Event e) {				
				if ((e.stateMask & SWT.CTRL) != 0) {
					int tab = e.character - '1';
					if (tab >= 0 && tab <= 8) {
						tabFolder.setSelection(tab);
					}
				}
		    }
		});
	}
	
	private void createIcons(int size) {
		int roundSize = size / 2;
		noneIcon = Utils.createFilledRoundRect(size, size, display.getSystemColor(SWT.COLOR_BLACK), roundSize);
		infoIcon = Utils.createFilledRoundRect(size, size, display.getSystemColor(SWT.COLOR_GREEN), roundSize);
		minorIcon = Utils.createFilledRoundRect(size, size, new Color(Display.getCurrent(), 255, 128, 0), roundSize);
		majorIcon = Utils.createFilledRoundRect(size, size, display.getSystemColor(SWT.COLOR_RED), roundSize);
		
		deviceOffIcon = noneIcon;
		deviceOnIcon = minorIcon;
	}
	
	private void createTray() {
		Tray tray = display.getSystemTray();
		if (tray != null) {
			trayItem = new TrayItem(tray, SWT.NONE);
			// Note: tray tool tip text is set in setTitle()
			
			Menu menu = new Menu(shell, SWT.POP_UP);
			
			MenuItem showItem = new MenuItem(menu, SWT.PUSH);
			showItem.setText("Show");
			showItem.addListener(SWT.Selection, new ShowListener());

			MenuItem hideItem = new MenuItem(menu, SWT.PUSH);
			hideItem.setText("Hide");
			hideItem.addListener(SWT.Selection, new HideListener());

			new MenuItem(menu, SWT.SEPARATOR);
			
			MenuItem exitItem = new MenuItem(menu, SWT.PUSH);
			exitItem.setText("Exit");
			exitItem.addListener(SWT.Selection, new ExitItemListener());

			trayItem.addListener(SWT.MenuDetect, new Listener() {
				@Override
				public void handleEvent(org.eclipse.swt.widgets.Event arg0) {
					menu.setVisible(true);
				}
			});
			
			/*
			trayItem.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
				}
			});
			*/
			
			trayItem.addListener(SWT.DefaultSelection, new ToggleVisibilityListener());
		}
		else {
			log.fine("No system tray available");
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
		
		MenuItem fileHideItem = new MenuItem(fileMenu, SWT.PUSH);
		fileHideItem.setText("Hide");
		fileHideItem.addListener(SWT.Selection, new HideListener());
		
		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");
		fileExitItem.addListener(SWT.Selection, new ExitItemListener());
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

		actionCameraStreamItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionCameraStreamItem.setText("Camera stream");
		
		actionGetEventImagesItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionGetEventImagesItem.setText("Show event images");
		actionGetEventImagesItem.addSelectionListener(new ActionGetEventImagesItemListener());

		actionSaveEventImagesItem = new MenuItem(actionMenu, SWT.CASCADE);
		actionSaveEventImagesItem.setText("Save event images...");
		actionSaveEventImagesItem.addSelectionListener(new ActionSaveEventImagesItemListener());

		new MenuItem(actionMenu, SWT.SEPARATOR);
		
		// Action -> Camera snapshot
		cameraSnapshotMenu = new Menu(shell, SWT.DROP_DOWN);
		actionCameraSnapshotItem.setMenu(cameraSnapshotMenu);
		
		// Action -> Camera stream
		cameraStreamMenu = new Menu(shell, SWT.DROP_DOWN);
		actionCameraStreamItem.setMenu(cameraStreamMenu);
		
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
		
		actionGetDataItem = new MenuItem(actionMenu, SWT.PUSH);
		actionGetDataItem.setText("Manually poll data");
		actionGetDataItem.addSelectionListener(new ActionGetSummaryItemListener());
		
		// Action -> Accept events
		Menu actionAcceptEventsMenu = new Menu(shell, SWT.DROP_DOWN);
		actionAcceptEventsItem.setMenu(actionAcceptEventsMenu);

		MenuItem actionAcceptAllEventsItem = new MenuItem(actionAcceptEventsMenu, SWT.PUSH);
		actionAcceptAllEventsItem.setText("All");
		actionAcceptAllEventsItem.addSelectionListener(new ActionAcceptAllEventsItemListener());

		MenuItem actionAcceptMajorEventsItem = new MenuItem(actionAcceptEventsMenu, SWT.PUSH);
		actionAcceptMajorEventsItem.setText("All with major severity");
		actionAcceptMajorEventsItem.addSelectionListener(new ActionAcceptMajorEventsItemListener());

		MenuItem actionAcceptMinorEventsItem = new MenuItem(actionAcceptEventsMenu, SWT.PUSH);
		actionAcceptMinorEventsItem.setText("All with minor severity");
		actionAcceptMinorEventsItem.addSelectionListener(new ActionAcceptMinorEventsItemListener());
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
		tabFolder = new TabFolder(shell, SWT.BORDER);
		createEventsTab();
	    createControlTab();
	    createWeatherTab();
	    createSensorsTab();
	    createLogTab();
	    createRemoteLogTab();
	}

	private void createEventsTab() {
		TabItem eventsTab = new TabItem(tabFolder, SWT.BORDER);
		eventsTab.setText("Events");

		eventsTable = new Table(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		eventsTable.setHeaderVisible(true);
		eventsTable.setLinesVisible(true);
		eventsTable.addMouseListener(new EventSelectedListener());
		
		TableColumn iconColumn = new TableColumn(eventsTable, SWT.NULL);
		iconColumn.setText("Icon");
		iconColumn.pack();
		
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

	private void createControlTab() {
	    controlTable = new Table(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
	    controlTable.setHeaderVisible(true);
	    controlTable.setLinesVisible(true);
	    controlTable.addMouseListener(new DeviceSelectedListener());

	    TabItem controlTab = new TabItem(tabFolder, SWT.BORDER);
	    controlTab.setText("Device Control");
	    controlTab.setToolTipText("Home automation device control");

	    TableColumn iconColumn = new TableColumn(controlTable, SWT.NULL);
	    iconColumn.setText("Icon");
	    iconColumn.pack();
	    
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
	
	private void createWeatherTab() {
	    weatherTable = new Table(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
	    weatherTable.setHeaderVisible(true);
	    weatherTable.setLinesVisible(true);

	    TabItem weatherTab = new TabItem(tabFolder, SWT.BORDER);
	    weatherTab.setText("Weather");
	    weatherTab.setToolTipText("Weather reports");
	    
	    TableColumn providerColumn = new TableColumn(weatherTable, SWT.NULL);
	    providerColumn.setText("Provider");
	    providerColumn.pack();

	    TableColumn areaColumn = new TableColumn(weatherTable, SWT.NULL);
	    areaColumn.setText("Area");
	    areaColumn.pack();

	    TableColumn updatedColumn = new TableColumn(weatherTable, SWT.NULL);
	    updatedColumn.setText("Updated");
	    updatedColumn.pack();

	    TableColumn validFromColumn = new TableColumn(weatherTable, SWT.NULL);
	    validFromColumn.setText("Valid from");
	    validFromColumn.pack();

	    TableColumn validToColumn = new TableColumn(weatherTable, SWT.NULL);
	    validToColumn.setText("Valid to");
	    validToColumn.pack();
	    
	    TableColumn temperatureColumn = new TableColumn(weatherTable, SWT.NULL);
	    temperatureColumn.setText("Temperature [Celsius]");
	    temperatureColumn.pack();
	    
	    TableColumn precipitationColumn = new TableColumn(weatherTable, SWT.NULL);
	    precipitationColumn.setText("Precipitation [mm/h]");
	    precipitationColumn.pack();
	    
	    TableColumn windColumn = new TableColumn(weatherTable, SWT.NULL);
	    windColumn.setText("Wind [m/s]");
	    windColumn.pack();

	    TableColumn pressureColumn = new TableColumn(weatherTable, SWT.NULL);
	    pressureColumn.setText("Pressure [hPa]");
	    pressureColumn.pack();

	    TableColumn sunriseColumn = new TableColumn(weatherTable, SWT.NULL);
	    sunriseColumn.setText("Sunrise");
	    sunriseColumn.pack();

	    TableColumn sunsetColumn = new TableColumn(weatherTable, SWT.NULL);
	    sunsetColumn.setText("Sunset");
	    sunsetColumn.pack();
	    
	    weatherTab.setControl(weatherTable);
	}
	
	private void createSensorsTab() {
		sensorsTable = new Table(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		sensorsTable.setHeaderVisible(true);
		sensorsTable.setLinesVisible(true);

	    TabItem sensorsTab = new TabItem(tabFolder, SWT.BORDER);
	    sensorsTab.setText("Sensors");
	    
	    TableColumn nameColumn = new TableColumn(sensorsTable, SWT.NULL);
	    nameColumn.setText("Name");
	    nameColumn.pack();
	    
	    TableColumn detailsColumn = new TableColumn(sensorsTable, SWT.NULL);
	    detailsColumn.setText("Details");
	    detailsColumn.pack();
	    
	    TableColumn updateFilterColumn = new TableColumn(sensorsTable, SWT.NULL);
	    updateFilterColumn.setText("Update filter");
	    updateFilterColumn.pack();

	    TableColumn triggerFilterColumn = new TableColumn(sensorsTable, SWT.NULL);
	    triggerFilterColumn.setText("Trigger filter");
	    triggerFilterColumn.pack();

	    TableColumn armedActionsColumn = new TableColumn(sensorsTable, SWT.NULL);
	    armedActionsColumn.setText("Armed actions");
	    armedActionsColumn.pack();

	    TableColumn disarmedActionsColumn = new TableColumn(sensorsTable, SWT.NULL);
	    disarmedActionsColumn.setText("Disarmed actions");
	    disarmedActionsColumn.pack();

	    TableColumn triggerCounterColumn = new TableColumn(sensorsTable, SWT.NULL);
	    triggerCounterColumn.setText("Trigger counter");
	    triggerCounterColumn.pack();

	    TableColumn mutedColumn = new TableColumn(sensorsTable, SWT.NULL);
	    mutedColumn.setText("Muted");
	    mutedColumn.pack();

	    TableColumn areasColumn = new TableColumn(sensorsTable, SWT.NULL);
	    areasColumn.setText("Areas");
	    areasColumn.pack();

	    sensorsTab.setControl(sensorsTable);
	}

	private void createLogTab() {
	    TabItem logTab = new TabItem(tabFolder, SWT.BORDER);
	    logTab.setText("Log");
	    
	    logText = new Text(tabFolder, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    logText.setEditable(false);
	    logTab.setControl(logText);
	}

	private void createRemoteLogTab() {
	    TabItem remoteLogTab = new TabItem(tabFolder, SWT.BORDER);
	    remoteLogTab.setText("Remote Log");
	    
	    remoteLogText = new Text(tabFolder, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    remoteLogText.setEditable(false);
	    remoteLogTab.setControl(remoteLogText);
	}
	
	private void updateGui() {
		
		boolean connected = connectedState == ConnectedState.CONNECTED;
		
		fileConnectItem.setEnabled(!connected);
		fileDisconnectItem.setEnabled(connected);
		fileSettingsItem.setEnabled(!connected);
		
		// Note: the armed state is unknown until summary XML/JSON has been fetched
		actionArmItem.setEnabled(connected);
		actionDisarmItem.setEnabled(connected);
		actionGetDataItem.setEnabled(connected);
		actionTurnOnAllDevicesItem.setEnabled(connected);
		actionTurnOffAllDevicesItem.setEnabled(connected);
		actionTurnOnDevicesItem.setEnabled(connected);
		actionTurnOffDevicesItem.setEnabled(connected);
		actionTemporaryDisarmItem.setEnabled(connected);
		actionAcceptEventsItem.setEnabled(connected);
		actionCameraSnapshotItem.setEnabled(connected);
		actionCameraStreamItem.setEnabled(connected);
		actionGetEventImagesItem.setEnabled(connected);
		actionSaveEventImagesItem.setEnabled(connected);

		updateCameras();
		
		setIcon(noneIcon);
		
		updateTitle();
	}
	
	private void clearTableData() {
		eventsTable.removeAll();
		controlTable.removeAll();
		weatherTable.removeAll();
		sensorsTable.removeAll();
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
		
		String site = model.getSite(null);
		String armedStr = model.getArmedStr(null);
		if (site != null && armedStr != null) {
			String apiWorkingStr;
			if (apiWorking) {
				apiWorkingStr = "working";
			}
			else {
				apiWorkingStr = "idle";
			}
			setTitle(NAME + " (" + connectedStr + ", " + site + " is " + armedStr + ", API-thread is " + apiWorkingStr + ")");
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
		Boolean armed = model.getArmed(null);
		if (armed != null) {
			actionArmItem.setEnabled(!armed);
			actionDisarmItem.setEnabled(armed);
			actionTemporaryDisarmItem.setEnabled(armed);
		}
	}

	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private class ExitItemListener implements Listener {
		@Override
		public void handleEvent(org.eclipse.swt.widgets.Event arg0) {
			shell.close();
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

	private class ActionGetSummaryItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			updateSummary();
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
	
	private class ActionCameraImageItemListener implements SelectionListener {
		private int cameraIndex;
		
		public ActionCameraImageItemListener(int cameraIndex) {
			this.cameraIndex = cameraIndex;
		}
		
		public void widgetSelected(SelectionEvent event) {
			apiThread.runTask(new GetCameraImageTask(cameraIndex, ImageType.SNAPSHOT));
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	private class ActionOpenStreamListener implements SelectionListener {
		private int cameraIndex;
		
		public ActionOpenStreamListener(int cameraIndex) {
			this.cameraIndex = cameraIndex;
		}
		
		public void widgetSelected(SelectionEvent event) {
			// Close and cleanup any existing stream window 
			for (CameraStreamWindow streamWindow : streamWindows) {
				if (streamWindow.getCameraIndex() == cameraIndex) {
					streamWindow.close();
					streamWindows.remove(streamWindow);
					break;
				}
			}

			// Open a new window and request the first image
			streamWindows.add(new CameraStreamWindow(model.getSite(""), cameraIndex));
			apiThread.runTask(new GetCameraImageTask(cameraIndex, ImageType.STREAM));
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	private class HelpAboutItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			new AboutWindow(shell, noneIcon, infoIcon, minorIcon, majorIcon, deviceOffIcon, deviceOnIcon).run();
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

	private class ShowListener implements Listener {
		@Override
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			shell.setVisible(true);
		}
	}

	private class HideListener implements Listener {
		@Override
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			shell.setVisible(false);
		}
	}
	
	private class ToggleVisibilityListener implements Listener {
		@Override
		public void handleEvent(org.eclipse.swt.widgets.Event event) {
			shell.setVisible(!shell.isVisible());
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
			boolean hashKnownHosts = connectWindow.getHashKnownHosts();
			boolean strictHostKeyChecking = connectWindow.getStrictHostKeyChecking();

			// Note that settings are written when a connection attempt has been done
			model.updateSettingsFromGui(host, port, protocol);
			
			apiThread.runTask(new ConnectTask(host, port, protocol, password, hashKnownHosts, strictHostKeyChecking, debug));
		}
		else {
			log.fine("Connect window has invalid input");
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

	private void updateSummary() {
		apiThread.runTask(new GetSummaryXmlTask());
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
			int deviceId = (int)selection.getData();
			Device device = model.getDevice(deviceId);
			if (device != null) {
				if (device.state) {
					apiThread.runTask(new TurnOffDeviceTask(device.id));
				}
				else {
					apiThread.runTask(new TurnOnDeviceTask(device.id));
				}
			}
		}
	}
	
	@Override
	public void updateConnectedState(ConnectedState state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				connectedState = state;
				
				if (state == ConnectedState.CONNECTED) {
					// Reset old fetched data when connected, cause it might be invalid for this host.
					model.reset();
					
					clearTableData();

					// Update cameras once to enable menu items
					apiThread.runTask(new GetCamerasXmlTask());
					
					getSummaryXmlRunnable.run();
				}
				
				updateGui();
			}
		});
	}

	@Override
	public void updateWeatherXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				log.info("Received weather.xml");
				
				if (model.updateFromWeatherXml(xml)) {
					weatherTable.setRedraw(false);
					weatherTable.removeAll();
					for (WeatherReport report : model.getWeatherReports()) {
						TableItem item = new TableItem(weatherTable, SWT.NULL);
						int col = 0;
						item.setText(col++, report.provider);
						item.setText(col++, report.area);
						item.setText(col++, report.updated);
						item.setText(col++, report.validFrom);
						item.setText(col++, report.validTo);
						item.setText(col++, report.getTemperatureStr());
						item.setText(col++, report.getPrecipitationStr());
						item.setText(col++, report.getWindspeedStr());
						item.setText(col++, report.getPressureStr());
						item.setText(col++, report.sunrise);
						item.setText(col++, report.sunset);
					}
					for (int i=0; i<weatherTable.getColumnCount(); i++) {
						weatherTable.getColumn(i).pack();
					}
					weatherTable.setRedraw(true);
					weatherTable.redraw();
				}
				else {
					openApiError("Received invalid weather.xml");
				}
			}
		});
	}
	
	@Override
	public void updateCamerasXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				log.info("Received cameras.xml");
				
				if (model.updateFromCamerasXml(xml)) {
					updateCameras();
				}
				else {
					openApiError("Received invalid cameras.xml");
				}
			}
		});
	}
	
	private void updateCameras() {
		MenuItem[] snapshotItems = cameraSnapshotMenu.getItems();
		for (MenuItem item : snapshotItems) {
		    item.dispose();
		}
		
		MenuItem[] streamItems = cameraStreamMenu.getItems();
		for (MenuItem item : streamItems) {
		    item.dispose();
		}
		
		for (CameraStreamWindow w : streamWindows) {
			w.close();
		}
		streamWindows.clear();
		
		for (Camera camera : model.getCameras()) {
			String cameraName = "Camera " + (camera.index+1) + " [" + camera.width + "x" + camera.height + "]";
			
			MenuItem snapshotMenuItem = new MenuItem(cameraSnapshotMenu, SWT.PUSH);
			snapshotMenuItem.setText(cameraName);
			snapshotMenuItem.addSelectionListener(new ActionCameraImageItemListener(camera.index));

			MenuItem streamMenuItem = new MenuItem(cameraStreamMenu, SWT.PUSH);
			streamMenuItem.setText(cameraName);
			streamMenuItem.addSelectionListener(new ActionOpenStreamListener(camera.index));
		}
	}
	
	@Override
	public void updateControlXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				log.info("Received control.xml");
				
				if (model.updateFromControlXml(xml)) {
					controlTable.setRedraw(false);
					controlTable.removeAll();
					for (Device device : model.getDevices()) {
						TableItem item = new TableItem(controlTable, SWT.NULL);
						int col = 0;
						if (device.state) {
							item.setImage(col++, deviceOnIcon);
						}
						else {
							item.setImage(col++, deviceOffIcon);
						}
						item.setText(col++, device.name);
						item.setText(col++, device.getStateStr());
						item.setText(col++, device.getTimeLeftStr());
						item.setText(col++, device.getTypeStr());
						item.setData(device.id);
					}
					for (int i=0; i<controlTable.getColumnCount(); i++) {
						controlTable.getColumn(i).pack();
					}
					controlTable.setRedraw(true);
					controlTable.redraw();
				}
				else {
					openApiError("Received invalid control.xml");
				}
			}
		});
	}
	
	@Override
	public void updateEventsXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				log.info("Received events.xml");
				
				long prevLatestEventId = model.getLatestEventId(-1l);
				if (model.updateFromEventsXml(xml)) {
					log.fine("Parsed events.xml");

					eventsTable.setRedraw(false);
					eventsTable.removeAll();
					
					List<Event> events = model.getEvents();
					
					if (!events.isEmpty()) {
						boolean newInfoEvent = false;
						boolean newMinorEvent = false;
						boolean newMajorEvent = false;
						
						ListIterator<Event> it = events.listIterator(events.size());
						while (it.hasPrevious()) {
							Event event = it.previous();
							
							if (event.id > prevLatestEventId) {
								switch (event.severity) {
								case INFO:
									newInfoEvent = true;
									break;
								case MINOR:
									newMinorEvent = true;
									break;
								case MAJOR:
									newMajorEvent = true;
									break;
								}
							}
							
							TableItem item = new TableItem(eventsTable, SWT.NULL);
							int col = 0;
							Image icon;
							switch (event.severity) {
							case INFO:
								icon = infoIcon;
								break;
							case MINOR:
								icon = minorIcon;
								break;
							case MAJOR:
								icon = majorIcon;
								break;
							default:
								icon = noneIcon;
								break;
							}
							item.setImage(col++, icon);
							item.setText(col++, String.valueOf(event.id));
							item.setText(col++, event.time);
							item.setText(col++, event.getSeverityStr());
							item.setText(col++, event.message);
							item.setText(col++, event.sensor);
							item.setText(col++, event.getArmedStr());
							item.setText(col++, String.valueOf(event.images));
							item.setData(event.id);
						}
						
						for (int i=0; i<eventsTable.getColumnCount(); i++) {
							eventsTable.getColumn(i).pack();
						}
						
						if (model.getSettings().notifyOnNewMajorEvent && newMajorEvent) {
							new Notification(model.getSite(""), "New event with major severity detected!", Severity.MAJOR);
						}
						else if (model.getSettings().notifyOnNewMinorEvent && newMinorEvent) {
							new Notification(model.getSite(""), "New event with minor severity detected!", Severity.MINOR);
						}
						else if (model.getSettings().notifyOnNewInfoEvent && newInfoEvent) {
							new Notification(model.getSite(""), "New event with info severity detected!", Severity.INFO);
						}
					}
					
					eventsTable.setRedraw(true);
					eventsTable.redraw();
				}
				else {
					openApiError("Received invalid events.xml");
				}
			}
		});
	}
	
	@Override
	public void updateSensorsXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				log.info("Received sensors.xml");
				
				if (model.updateFromSensorsXml(xml)) {
					log.fine("Parsed cameras.xml");
					
					sensorsTable.setRedraw(false);
					sensorsTable.removeAll();
					
					List<Sensor> sensors = model.getSensors();
					
					if (!sensors.isEmpty()) {
						for (Sensor sensor : sensors) {
							TableItem item = new TableItem(sensorsTable, SWT.NULL);
							int col = 0;
							item.setText(col++, sensor.name);
							item.setText(col++, sensor.details);
							item.setText(col++, sensor.updateFilter);
							item.setText(col++, sensor.triggerFilter);
							item.setText(col++, String.valueOf(sensor.armedActions));
							item.setText(col++, String.valueOf(sensor.disarmedActions));
							item.setText(col++, String.valueOf(sensor.triggerCount));
							item.setText(col++, String.valueOf(sensor.muted));
							item.setText(col++, sensor.areas);
						}
					}
					
					for (int i=0; i<sensorsTable.getColumnCount(); i++) {
						sensorsTable.getColumn(i).pack();
					}
					
					sensorsTable.setRedraw(true);
					sensorsTable.redraw();
				}
				else {
					openApiError("Received invalid sensors.xml");
				}
			}
		});
	}
	
	@Override
	public void updateLogXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (model.updateFromLogXml(xml)) {
					remoteLogText.setText(model.getLogText());
				}
				else {
					openApiError("Received invalid log.xml");
				}
			}
		});
	}

	@Override
	public void updateSummaryXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				log.info("Received summary.xml");
				
				long prevLatestEventId = model.getLatestEventId(-1l);
				int prevNumInfoEvents = model.getNumInfoEvents(0);
				int prevNumMinorEvents = model.getNumMinorEvents(0);
				int prevNumMajorEvents = model.getNumMajorEvents(0);
				String prevControlChecksum = model.getControlChecksum("");
				String prevWeatherChecksum = model.getWeatherChecksum("");
				Double prevLogTimestamp = model.getLogTimestamp(0.0);
				
				if (model.updateFromSummaryXml(xml)) {
					updateTitle();
					updateArmMenuItems();
					
					if (model.getNumMajorEvents(0) > 0) {
						setIcon(majorIcon);
					}
					else if (model.getNumMinorEvents(0) > 0) {
						setIcon(minorIcon);
					}
					else if (model.getNumInfoEvents(0) > 0) {
						setIcon(infoIcon);
					}
					else {
						setIcon(noneIcon);
					}
					
					if (prevLatestEventId != model.getLatestEventId(-1l) ||
							prevNumInfoEvents != model.getNumInfoEvents(0) ||
							prevNumMinorEvents != model.getNumMinorEvents(0) ||
							prevNumMajorEvents != model.getNumMajorEvents(0)) {
						if (model.getSettings().getNewEvents) {
							log.info("Event change detected, requesting events");
							apiThread.runTask(new GetEventsXmlTask());
						}
					}
					
					if (!prevControlChecksum.equals(model.getControlChecksum(""))) {
						if (model.getSettings().getNewControl) {
							log.info("New control checksum detected, requesting control");
							apiThread.runTask(new GetControlXmlTask());
						}
					}

					if (!prevWeatherChecksum.equals(model.getWeatherChecksum(""))) {
						if (model.getSettings().getNewWeather) {
							log.info("New weather checksum detected, requesting weather");
							apiThread.runTask(new GetWeatherXmlTask());
						}
					}

					// TODO: add total sensor trigger count to API
					if (model.getSettings().getNewSensors) {
						log.info("New sensor trigger sum detected, requesting sensors");
						apiThread.runTask(new GetSensorsXmlTask());
					}
					
					if (prevLogTimestamp.compareTo(model.getLogTimestamp(0.0)) != 0) {
						if (model.getSettings().getNewLog) {
							log.info("New log timestamp detected, requesting log");
							apiThread.runTask(new GetLogXmlTask());
						}
					}
				}
				else {
					openApiError("Received invalid summary.xml");
				}

				display.timerExec(model.getSettings().summaryPollInterval*1000, getSummaryXmlRunnable);
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
					String windowTitle = model.getSite("") +  ": Event " + eventId + " (image " + (imageIndex+1) + ")";
					Image image = Utils.createImageFromJpegData(jpegData);
					if (image != null) {
						new ImageWindow(windowTitle, image);
					}
					else {
						openApiError("Received invalid jpeg data");
					}
				}
				
				if (save) {
					String filename = imagesDirectory + File.separatorChar + Settings.createFilenameForEventImage(eventId, imageIndex);
					FileWriter.saveFile(filename, jpegData);
				}
			}
		});
	}
	
	@Override
	public void updateCameraSnapshot(int cameraIndex, byte[] jpegData) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				String title = model.getSite("") + ": Camera " + (cameraIndex+1) + " snapshot";
				Image image = Utils.createImageFromJpegData(jpegData);
				if (image != null) {
					new ImageWindow(title, image);
				}
				else {
					openApiError("Received invalid jpeg data");
				}
			}
		});
	}

	@Override
	public void updateCameraStream(int cameraIndex, byte[] jpegData) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				for (CameraStreamWindow streamWindow : streamWindows) {
					if (streamWindow.getCameraIndex() == cameraIndex) {
						if (streamWindow.isOpen()) {
							Image image = Utils.createImageFromJpegData(jpegData);
							if (image != null) {
								streamWindow.updateImage(image);
								apiThread.runTask(new GetCameraImageTask(cameraIndex, ImageType.STREAM));
							}
							else {
								openApiError("Received invalid jpeg data");
							}
						}
					}
				}
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
				openApiError(message);
			}
		});
	}
	
	private void openApiError(String message) {
		openError(API_ERROR_NAME, message);
	}
	
	private void openError(String title, String message) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		dialog.setText(title);
		dialog.setMessage(message);
		dialog.open();
	}
}
