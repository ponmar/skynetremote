package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
	private Image oldImage = null; 

	private int mouseDownX = -1;
	private int mouseDownY = -1;
	
	private static Point position = null;
	
	public Notification(String title, String message, Severity severity) {
		shell = new Shell(Display.getDefault(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		
		// Make widgets have transparent background
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		shell.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
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
				
				Image newImage = Utils.createFadedImage(width, height, fgColor, bgColor, edgeWith);
		        
		        // now set the background image on the shell 
		        shell.setBackgroundImage(newImage); 
		 
		        if (oldImage != null) { 
		        	oldImage.dispose(); 
		        } 
		        oldImage = newImage;
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
		
		// TODO: how to catch mouse events for labels?
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
				if (mouseDownX != -1 && mouseDownY != -1) {
					int xMovement = e.x - mouseDownX;
					int yMovement = e.y - mouseDownY;
					position = new Point(shell.getLocation().x + xMovement, shell.getLocation().y + yMovement);
					shell.setLocation(position);
					mouseDownX = -1;
					mouseDownY = -1;
				}
			}
		});

		CLabel titleLabel = new CLabel(shell, SWT.NONE);
		FontData[] fD = titleLabel.getFont().getFontData();
		if (fD.length > 0) {
			fD[0].setHeight(HEADING_FONT_SIZE);
			headingFont = new Font(Display.getDefault(), fD[0]);
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

		shell.pack();
		shell.open();
		shell.forceActive();
	}
	
	private void hide() {
		shell.close();
	}
}
