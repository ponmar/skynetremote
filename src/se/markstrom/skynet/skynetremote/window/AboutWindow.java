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
	
	public AboutWindow(Shell parentShell, Image noneIcon, Image infoIcon, Image minorIcon, Image majorIcon, Image deviceOffIcon, Image deviceOnIcon) {
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

		Label windowIconsHeading = new Label(shell, SWT.NONE);
		windowIconsHeading.setText("Window icons:");
		windowIconsHeading.pack();
		
		new Label(shell, SWT.NONE);
		
		Label noneIconLabel = new Label(shell, SWT.NONE);
		noneIconLabel.setImage(noneIcon);

		Label noneIconText = new Label(shell, SWT.NONE);
		noneIconText.setText("No event information has been fetched");
		noneIconText.pack();
		
		Label infoIconLabel = new Label(shell, SWT.NONE);
		infoIconLabel.setImage(infoIcon);

		Label infoIconText = new Label(shell, SWT.NONE);
		infoIconText.setText("There are only info events");
		infoIconText.pack();
		
		Label minorIconLabel = new Label(shell, SWT.NONE);
		minorIconLabel.setImage(minorIcon);

		Label minorIconText = new Label(shell, SWT.NONE);
		minorIconText.setText("There are minor events");
		minorIconText.pack();

		Label majorIconLabel = new Label(shell, SWT.NONE);
		majorIconLabel.setImage(majorIcon);

		Label majorIconText = new Label(shell, SWT.NONE);
		majorIconText.setText("There are major events");
		majorIconText.pack();

		Label eventIconsHeading = new Label(shell, SWT.NONE);
		eventIconsHeading.setText("Event icons:");
		eventIconsHeading.pack();
		
		new Label(shell, SWT.NONE);
		
		Label eventInfoIconLabel = new Label(shell, SWT.NONE);
		eventInfoIconLabel.setImage(infoIcon);

		Label eventInfoIconText = new Label(shell, SWT.NONE);
		eventInfoIconText.setText("Info event severity");
		eventInfoIconText.pack();
		
		Label eventMinorIconLabel = new Label(shell, SWT.NONE);
		eventMinorIconLabel.setImage(minorIcon);

		Label eventMinorIconText = new Label(shell, SWT.NONE);
		eventMinorIconText.setText("Minor event severity");
		eventMinorIconText.pack();

		Label eventMajorIconLabel = new Label(shell, SWT.NONE);
		eventMajorIconLabel.setImage(majorIcon);

		Label eventMajorIconText = new Label(shell, SWT.NONE);
		eventMajorIconText.setText("Major event severity");
		eventMajorIconText.pack();
		
		Label deviceIconsHeading = new Label(shell, SWT.NONE);
		deviceIconsHeading.setText("Device icons:");
		deviceIconsHeading.pack();
		
		new Label(shell, SWT.NONE);
		
		Label deviceOffIconLabel = new Label(shell, SWT.NONE);
		deviceOffIconLabel.setImage(deviceOffIcon);
		
		Label deviceOffIconText = new Label(shell, SWT.NONE);
		deviceOffIconText.setText("The device is turned off");
		deviceOffIconText.pack();

		Label deviceOnIconLabel = new Label(shell, SWT.NONE);
		deviceOnIconLabel.setImage(deviceOnIcon);
		
		Label deviceOnIconText = new Label(shell, SWT.NONE);
		deviceOnIconText.setText("The device is turned on");
		deviceOnIconText.pack();

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
