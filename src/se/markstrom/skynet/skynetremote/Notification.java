package se.markstrom.skynet.skynetremote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A notification area based on example at:
 * http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
 */
public class Notification {

	private static final int WIDTH = 200;
	private static final int HEIGHT = 100;
	
	private Shell shell;
	private int mouseDownX = -1;
	private int mouseDownY = -1;
	
	private static Point position = null;
	
	public Notification(String title, String message) {
		shell = new Shell(Display.getDefault(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setSize(WIDTH, HEIGHT);
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		shell.setLayout(layout);
		
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

		// TODO: use CLabel for heading, as in http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
		Label titleLabel = new Label(shell, SWT.WRAP);
		titleLabel.setText(title);
		titleLabel.pack();
		
		Label messageLabel = new Label(shell, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.pack();

		shell.open();
		shell.forceActive();
	}
	
	private void hide() {
		shell.close();
	}
}
