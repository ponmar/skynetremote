package se.markstrom.skynet.skynetremote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AboutWindow {
	
	private Display display;
	private Shell shell;
	
	public AboutWindow() {
		display = Display.getDefault();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("About");
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 30;
		layout.marginLeft = 30;
		layout.marginRight = 30;
		layout.marginBottom = 30;
		shell.setLayout(layout);

		Label intro = new Label(shell, SWT.NONE);
		intro.setText("This program implements many features available in the Skynet API.");
		intro.pack();

		Label author = new Label(shell, SWT.NONE);
		author.setText("Author: pontus.markstrom@gmail.com");
		author.pack();
		
		// TODO: add link to bitbucket project when it is public

		shell.pack();
		shell.open();
	}
	
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
