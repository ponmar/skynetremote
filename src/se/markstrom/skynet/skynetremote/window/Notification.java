package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import se.markstrom.skynet.skynetremote.model.Event.Severity;

public class Notification {

	private static final int HEADING_FONT_SIZE = 14;
	
	private Shell shell;
	private Font headingFont = null;
	private Image backgroundImage = null;

	private int mouseDownX = -1;
	private int mouseDownY = -1;
	
	private boolean mouseButtonDown = false;
	
	private static Point position = null;
	
	public Notification(String title, String message, Severity severity) {
		shell = new Shell(Display.getDefault(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		
		// Make widgets have transparent background
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		shell.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event arg0) {

				// Generate background at first resize
				if (backgroundImage == null) {
					Rectangle rect = shell.getClientArea();
					
					int width = Math.max(1, rect.width);
					int height = rect.height;
					int edgeWith = 2;
	
					Color fgColor = null;
					switch (severity) {
					case INFO:
						fgColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
						break;
					case MINOR:
						fgColor = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
						break;
					case MAJOR:
						fgColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
						break;
					}
					Color bgColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
					
					backgroundImage = Utils.createFadedImage(width, height, fgColor, bgColor, edgeWith);
			        shell.setBackgroundImage(backgroundImage); 
				}
			}
		});
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 20;
		layout.marginLeft = 40;
		layout.marginRight = 40;
		layout.marginBottom = 20;
		shell.setLayout(layout);
		
		if (position == null) {
			position = shell.getLocation();
		}
		shell.setLocation(position);
		
		NotificationMouseMoveListener mouseMoveListener = new NotificationMouseMoveListener(); 
		NotificationMouseListener mouseListener = new NotificationMouseListener();
		shell.addMouseMoveListener(mouseMoveListener);
		shell.addMouseListener(mouseListener);

		CLabel titleLabel = new CLabel(shell, SWT.NONE);
		titleLabel.addMouseListener(mouseListener);
		titleLabel.addMouseMoveListener(mouseMoveListener);
		
		FontData[] fontData = titleLabel.getFont().getFontData();
		if (fontData.length > 0) {
			fontData[0].setHeight(HEADING_FONT_SIZE);
			headingFont = new Font(Display.getDefault(), fontData[0]);
			titleLabel.setFont(headingFont);
			titleLabel.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					headingFont.dispose();
				}
			});
		}
		titleLabel.setText(title);
		titleLabel.pack();

		Label messageLabel = new Label(shell, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.pack();
		messageLabel.addMouseListener(mouseListener);
		messageLabel.addMouseMoveListener(mouseMoveListener);

		shell.pack();
		shell.open();
		shell.forceActive();
	}
	
	private void hide() {
		shell.close();
		if (backgroundImage != null) {
			backgroundImage.dispose();
		}
	}
	
	private class NotificationMouseListener implements MouseListener {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			hide();
		}

		@Override
		public void mouseDown(MouseEvent e) {
			mouseDownX = e.x;
			mouseDownY = e.y;
			mouseButtonDown = true;
		}

		@Override
		public void mouseUp(MouseEvent e) {
			mouseButtonDown = false;
		}
	}
	
	private class NotificationMouseMoveListener implements MouseMoveListener {
		@Override
		public void mouseMove(MouseEvent e) {
			if (mouseButtonDown) {
				position.x = shell.getLocation().x + (e.x - mouseDownX); 
				position.y = shell.getLocation().y + (e.y - mouseDownY);
				shell.setLocation(position);
			}
		}
	}
}
