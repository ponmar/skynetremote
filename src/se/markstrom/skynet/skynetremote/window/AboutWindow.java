package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AboutWindow {
	
	private Display display;
	private Shell shell;
	
	public AboutWindow(Shell parentShell) {
		display = Display.getDefault();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("About");
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 30;
		layout.marginLeft = 30;
		layout.marginRight = 30;
		layout.marginBottom = 30;
		shell.setLayout(layout);

		Label author = new Label(shell, SWT.NONE);
		author.setText("Author: pontus.markstrom@gmail.com");
		author.pack();

		Label site = new Label(shell, SWT.NONE);
		site.setText("Site: https://bitbucket.org/pontusmarkstrom/skynet-remote/");
		site.pack();

		shell.addTraverseListener(new KeyListener());
		
		shell.pack();
		
		// Center window
		Point parentLocation = parentShell.getLocation();
		Point parentSize = parentShell.getSize();
		Point dialogSize = shell.getSize();
		shell.setLocation(
				parentLocation.x + (parentSize.x - dialogSize.x) / 2,
				parentLocation.y + (parentSize.y - dialogSize.y) / 2);
		
		shell.open();
	}
	
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private class KeyListener implements TraverseListener {
		@Override
		public void keyTraversed(TraverseEvent event) {
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
				shell.dispose();
				break;
			}
		}
	}
}
