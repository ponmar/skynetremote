package se.markstrom.skynet.skynetremote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A notification area based on example at:
 * http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
 */
public class Notification {

	private Shell shell;
	private int mouseDownX = -1;
	private int mouseDownY = -1;
	
	private static Point position = null;
	
	private Label messageLabel;

	public Notification(String message) {
		// Display.getDefault().getActiveShell() can also be used
		shell = new Shell(Display.getDefault(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setSize(200, 100);
		
		if (position == null) {
			position = shell.getLocation();
		}
		shell.setLocation(position);
		
		shell.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				hide();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				mouseDownX = e.x;
				mouseDownY = e.y;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				int xMovement = e.x - mouseDownX;
				int yMovement = e.y - mouseDownY;
				position = new Point(shell.getLocation().x + xMovement, shell.getLocation().y + yMovement);
				shell.setLocation(position);
			}
		});

		messageLabel = new Label(shell, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.pack();

		shell.open();
		shell.forceActive();
	}
	
	private void hide() {
		shell.close();
	}
}
