package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ImageWindow {
	private Display display;
	private Shell shell;
	
	public ImageWindow(String title, Image image) {
		createGui(title, image);
	}
	
	private void createGui(String title, Image image) {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setText(title);
		
		Label label = new Label(shell, SWT.BORDER);
		label.setImage(image);
		label.pack();
		
		shell.addTraverseListener(new EscapeKeyListener());
		shell.pack();
		shell.open();		
	}
	
	private void close() {
		shell.dispose();
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
