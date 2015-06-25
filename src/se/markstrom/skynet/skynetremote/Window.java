package se.markstrom.skynet.skynetremote;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
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
import se.markstrom.skynet.skynetremote.apitask.GetEventsXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetLogXmlTask;
import se.markstrom.skynet.skynetremote.apitask.GetSummaryXmlTask;
import se.markstrom.skynet.skynetremote.xmlparsing.Event;
import se.markstrom.skynet.skynetremote.xmlparsing.EventsXmlParser;
import se.markstrom.skynet.skynetremote.xmlparsing.LogXmlParser;
import se.markstrom.skynet.skynetremote.xmlparsing.SummaryXmlParser;

public class Window implements GUI {
	
	private static final String TITLE = "Skynet Remote";
	private static final int SUMMARY_TIME = 5000;
	
	private Display display;
	private Shell shell;
	private MenuItem fileConnectItem;
	private MenuItem fileDisconnectItem;
	private MenuItem actionArmItem;
	private MenuItem actionDisarmItem;
	private Table eventsTable;
	private Text logText;
	
	private double prevLogTimestamp = 0; 
	private int prevLatestEventId = -1;
	
	private ApiThread apiThread = new ApiThread(this);
	
	public Window() {
		createGui();
		apiThread.start();
	}
	
	public void close() {
		apiThread.close();
	}

	private void createGui() {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setSize(800, 600);
		
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

		shell.setLayout(new FillLayout());
	
		// Tabs
		TabFolder tf = new TabFolder(shell, SWT.BORDER);
		
	    TabItem eventsTab = new TabItem(tf, SWT.BORDER);
	    eventsTab.setText("Events");
	    eventsTable = new Table(tf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    eventsTable.setHeaderVisible(true);
	    eventsTable.setLinesVisible(true);
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
	    eventsTab.setControl(eventsTable);
	    //eventsTableEditor = new TableEditor(eventsTable);

	    TabItem streamingTab = new TabItem(tf, SWT.BORDER);
	    streamingTab.setText("Live Streaming");

	    TabItem controlTab = new TabItem(tf, SWT.BORDER);
	    controlTab.setText("Control");

	    TabItem logTab = new TabItem(tf, SWT.BORDER);
	    logTab.setText("Log");
	    
	    logText = new Text(tf, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    logText.setEditable(false);
	    logTab.setControl(logText);	    
	    
	    shell.setMenuBar(menuBar);
		shell.open();
		
		updateConnectedMenuItems(false);
		
		startSummaryXmlTimer();
	}
	
	private void updateConnectedMenuItems(boolean connectedState) {
		fileConnectItem.setEnabled(!connectedState);
		fileDisconnectItem.setEnabled(connectedState);
		
		// Note: the armed state is unknown until summary XML/JSON has been fetched
		actionArmItem.setEnabled(connectedState);
		actionDisarmItem.setEnabled(connectedState);
		
		if (connectedState) {
			shell.setText(TITLE + " (connected)");
		}
		else {
			shell.setText(TITLE + " (disconnected)");
		}
	}
	
	private void updateArmMenuItems(boolean armedState) {
		actionArmItem.setEnabled(!armedState);
		actionDisarmItem.setEnabled(armedState);
	}
	
	private void startSummaryXmlTimer() {
		Runnable timer = new Runnable() {
			public void run() {
				apiThread.runTask(new GetSummaryXmlTask());
				display.timerExec(SUMMARY_TIME, this);
			}
		};
		display.timerExec(SUMMARY_TIME, timer);
	}
	
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
	
	class FileExitItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			shell.close();
			display.dispose();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			shell.close();
			display.dispose();
		}
	}

	class FileConnectItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			connect();			
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	class FileDisconnectItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			disconnect();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}

	class ActionArmItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			arm();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
		}
	}
	
	class ActionDisarmItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			disarm();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
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

	@Override
	public void updateConnectedState(boolean state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				updateConnectedMenuItems(state);
			}
		});
	}
	
	@Override
	public void updateArmState(boolean state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				updateArmMenuItems(state);
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
					ListIterator<Event> it = events.listIterator(events.size());
					
					while (it.hasPrevious()) {
						Event event = it.previous();
						TableItem item = new TableItem(eventsTable, SWT.NULL);
						item.setText(0, String.valueOf(event.id));
						item.setText(1, event.time);
						item.setText(2, event.getSeverityStr());
						item.setText(3, event.message);
						item.setText(4, event.sensor);
					}
					
					for (int i=0; i<eventsTable.getColumnCount(); i++) {
						eventsTable.getColumn(i).pack();
					}
					
					eventsTable.setRedraw(true);
					eventsTable.redraw();
				}
			}
		});
	}
	
	@Override
	public void updateLogXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				//System.out.println("New log.xml: " + xml);
				LogXmlParser parser = new LogXmlParser(xml);
				if (parser.isValid()) {
					logText.setText(parser.getLogText());
				}
			}
		});
	}

	@Override
	public void updateSummaryJson(String json) {
	}

	@Override
	public void updateSummaryXml(String xml) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				SummaryXmlParser parser = new SummaryXmlParser(xml);
				if (parser.isValid()) {
					if (prevLatestEventId != parser.getLatestEventId()) {
						prevLatestEventId = parser.getLatestEventId();
						System.out.println("New events!");
						apiThread.runTask(new GetEventsXmlTask());
					}

					if (prevLogTimestamp != parser.getLogTimestamp()) {
						prevLogTimestamp = parser.getLogTimestamp();
						//apiThread.runTask(new GetLogXmlTask());
					}
				}
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
		Window window = new Window();
		window.run();
		window.close();
	}
}
