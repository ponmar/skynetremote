package se.markstrom.skynet.skynetremote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import se.markstrom.skynet.api.SkynetAPI;
import se.markstrom.skynet.skynetremote.apitask.ApiThread;
import se.markstrom.skynet.skynetremote.apitask.ConnectTask;

public class Window implements GUI {
	
	private Display display;
	private Shell shell;
	private MenuItem fileConnectItem;
	private MenuItem fileDisconnectItem;
	
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
		shell.setText("Skynet Remote");
		shell.setSize(800, 600);
		
		Menu menuBar = new Menu(shell, SWT.BAR);
		
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
		
		shell.setLayout(new FillLayout());
		//shell.setLayout(new RowLayout());
		// TODO: create generic row
		
		// TODO: create tabs
		TabFolder tf = new TabFolder(shell, SWT.BORDER);
		
	    TabItem ti1 = new TabItem(tf, SWT.BORDER);
	    ti1.setText("Summary");
	    //ti1.setControl(new GroupExample(tf, SWT.SHADOW_ETCHED_IN));

	    TabItem ti2 = new TabItem(tf, SWT.BORDER);
	    ti2.setText("Events");
	    //ti2.setControl(new GridComposite(tf));

	    TabItem ti3 = new TabItem(tf, SWT.BORDER);
	    ti3.setText("Live Streaming");
	    //ti3.setControl(new GridComposite(tf));

	    TabItem ti4 = new TabItem(tf, SWT.BORDER);
	    ti4.setText("Control");
	    //ti4.setControl(new GridComposite(tf));
	    
	    shell.setMenuBar(menuBar);
		shell.open();
		
		updateFileMenuItems(false);
	}
	
	private void updateFileMenuItems(boolean connectedState) {
		if (connectedState) {
			fileConnectItem.setEnabled(false);
			fileDisconnectItem.setEnabled(true);
		}
		else {
			fileConnectItem.setEnabled(true);
			fileDisconnectItem.setEnabled(false);
		}
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
			connect();
		}
	}

	class FileDisconnectItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			disconnect();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			disconnect();
		}
	}
	
	private void connect() {
		if (!apiThread.isConnected()) {
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
	}

	private void disconnect() {
		if (apiThread.isConnected()) {
			apiThread.close();
		}
	}

	public static void main(String[] args) {
		Window window = new Window();
		window.run();
		window.close();
	}

	@Override
	public void updateConnectedState(boolean state) {
		display.asyncExec(new Runnable() {
			@Override
			public void run()
			{
				updateFileMenuItems(state);
			}
		});
	}
}
