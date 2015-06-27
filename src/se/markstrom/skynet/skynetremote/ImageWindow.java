package se.markstrom.skynet.skynetremote;

import java.io.ByteArrayInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ImageWindow {
	private Display display;
	private Shell shell;
	
	public ImageWindow(String title, byte [] jpegData) {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setText(title);
		
		ImageData imageData = new ImageData(new ByteArrayInputStream(jpegData));
		Image image = new Image(display, imageData);
		
		Label label = new Label(shell, SWT.BORDER);
		label.setImage(image);
		label.pack();
		
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
