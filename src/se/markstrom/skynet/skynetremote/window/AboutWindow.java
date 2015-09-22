package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AboutWindow {
	
	private static final int MARGIN = 30;
	private Display display;
	private Shell shell;
	
	public AboutWindow(Shell parentShell, Image noneIcon, Image infoIcon, Image minorIcon, Image majorIcon) {
		display = Display.getDefault();
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("About");
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = MARGIN;
		layout.marginLeft = MARGIN;
		layout.marginRight = MARGIN;
		layout.marginBottom = MARGIN;
		shell.setLayout(layout);

		Label authorHeading = new Label(shell, SWT.NONE);
		authorHeading.setText("Author:");
		authorHeading.pack();

		Label author = new Label(shell, SWT.NONE);
		author.setText("pontus.markstrom@gmail.com");
		author.pack();

		Label siteHeading = new Label(shell, SWT.NONE);
		siteHeading.setText("Site:");
		siteHeading.pack();

		Label site = new Label(shell, SWT.NONE);
		site.setText("https://bitbucket.org/pontusmarkstrom/skynet-remote/");
		site.pack();

		Label tabHeading = new Label(shell, SWT.NONE);
		tabHeading.setText("Select tab:");
		tabHeading.pack();

		Label tab = new Label(shell, SWT.NONE);
		tab.setText("CTRL + 1-9");
		tab.pack();

		Label closeHeading = new Label(shell, SWT.NONE);
		closeHeading.setText("Close windows:");
		closeHeading.pack();

		Label close = new Label(shell, SWT.NONE);
		close.setText("Escape");
		close.pack();

		Label noneIconLabel = new Label(shell, SWT.NONE);
		noneIconLabel.setImage(noneIcon);

		Label noneIconText = new Label(shell, SWT.NONE);
		noneIconText.setText("The icon used when no event information has been fetched");
		
		Label infoIconLabel = new Label(shell, SWT.NONE);
		infoIconLabel.setImage(infoIcon);

		Label infoIconText = new Label(shell, SWT.NONE);
		infoIconText.setText("The icon used when there are only info events");
		
		Label minorIconLabel = new Label(shell, SWT.NONE);
		minorIconLabel.setImage(minorIcon);

		Label minorIconText = new Label(shell, SWT.NONE);
		minorIconText.setText("The icon used when there are minor events");

		Label majorIconLabel = new Label(shell, SWT.NONE);
		majorIconLabel.setImage(majorIcon);

		Label majorIconText = new Label(shell, SWT.NONE);
		majorIconText.setText("The icon used when there are major events");

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
