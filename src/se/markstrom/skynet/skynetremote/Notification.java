package se.markstrom.skynet.skynetremote;

import javafx.scene.paint.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A notification area based on example at:
 * http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
 */
public class Notification {

	private Shell shell;
	private int mouseDownX = -1;
	private int mouseDownY = -1;
	
	/*
	private Color _borderColor = Color.ALICEBLUE;
	private Color _bgFgGradient = Color.BLUE;
	private Color _bgBgGradient = Color.CYAN;
	*/
	
	private Label messageLabel;

	public Notification(String message) {
		// Display.getDefault().getActiveShell() can also be used
		shell = new Shell(Display.getDefault(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setSize(200, 100); // TODO: fill with content and pack instead?
		//TODO: set shell location to previously used location
		
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
				shell.setLocation(shell.getLocation().x + xMovement, shell.getLocation().y + yMovement);
				// TODO: save new position to static variable or in Settings.
			}
		});

		messageLabel = new Label(shell, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.pack();
		
		/*
		shell.addListener(SWT.Resize, new Listener() { 
			@Override 
			public void handleEvent(Event e) { 
				try { 
					// get the size of the drawing area 
					Rectangle rect = shell.getClientArea();                     
					// create a new image with that size 
					Image newImage = new Image(Display.getDefault(), Math.max(1, rect.width), rect.height); 
					// create a GC object we can use to draw with 
					GC gc = new GC(newImage); 

					// fill background 
					//gc.setForeground(_bgFgGradient); 
					//gc.setBackground(_bgBgGradient); 
					gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true); 

					// draw shell edge 
					gc.setLineWidth(2);
					//gc.setForeground(_borderColor); 
					gc.drawRectangle(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2); 
					// remember to dipose the GC object! 
					gc.dispose(); 

					// now set the background image on the shell 
					shell.setBackgroundImage(newImage); 

					// remember/dispose old used iamge 
					/*
					if (_oldImage != null) { 
						_oldImage.dispose(); 
					} 
					_oldImage = newImage;
					*/
		/*
				} 
				catch (Exception err) { 
					err.printStackTrace(); 
				} 
			}
		});
		 */
		
		shell.open();
		shell.forceActive();
	}
	
	private void hide() {
		shell.close();
	}
}
