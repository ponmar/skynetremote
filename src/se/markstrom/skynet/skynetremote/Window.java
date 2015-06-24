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

public class Window {
	
	private Display display;
	private Shell shell;
	
	public Window() {
		createGui();
	}

	private void createGui() {
		display = new Display();
		shell = new Shell(display);

		Menu menuBar = new Menu(shell, SWT.BAR);
		
		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");
		
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
		
		MenuItem fileConnectItem = new MenuItem(fileMenu, SWT.PUSH);
		fileConnectItem.setText("&Connect...");

		MenuItem fileDisconnectItem = new MenuItem(fileMenu, SWT.PUSH);
		fileDisconnectItem.setText("&Disconnect");

		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");

		fileExitItem.addSelectionListener(new FileExitItemListener());
		
		shell.setText("Skynet Remote");
		shell.setSize(800, 600);

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
	}
	
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
	
	public static void main(String[] args) {
		new Window().run();
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
}
