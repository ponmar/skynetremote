package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class CameraStreamWindow {
	
	private int cameraIndex;
	private Display display;
	private Shell shell;
	private Label imageLabel;
	
	public CameraStreamWindow(String site, int cameraIndex) {
		this.cameraIndex = cameraIndex;
		createGui(site);
	}
	
	private void createGui(String site) {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setText(site + ": Camera " + (cameraIndex+1));

		imageLabel = new Label(shell, SWT.BORDER);
		
		shell.addTraverseListener(new EscapeKeyListener());
		shell.setVisible(false);
		shell.open();
	}

	// TODO: convert to Image outside this method and show error dialog when failed?
	public void updateImage(byte [] jpegData) {
		Image image = Utils.createImageFromJpegData(jpegData);
		if (image != null) {
			Image oldImage = imageLabel.getImage();
			imageLabel.setImage(image);
			imageLabel.pack();
			shell.pack();
			if (oldImage != null) {
				oldImage.dispose();
			}
		}
	}
	
	public boolean isOpen() {
		return !shell.isDisposed();
	}
	
	public void close() {
		if (!shell.isDisposed()) {
			shell.dispose();
		}
	}
	
	public int getCameraIndex() {
		return cameraIndex;
	}
	
	private class EscapeKeyListener implements TraverseListener {
		@Override
		public void keyTraversed(TraverseEvent event) {
			switch (event.detail) {
			case SWT.TRAVERSE_ESCAPE:
				close();
				break;
			}
		}
	}
}
