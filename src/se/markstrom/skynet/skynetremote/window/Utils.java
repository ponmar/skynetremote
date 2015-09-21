package se.markstrom.skynet.skynetremote.window;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Utils {
	
	static Image createFadedImage(int width, int height, Color fgColor, Color bgColor, int edgeWith) {
		Image image = new Image(Display.getDefault(), width, height);
		GC gc = new GC(image);

		// Draw background
        gc.setForeground(fgColor); 
        gc.setBackground(bgColor);
        gc.fillGradientRectangle(0, 0, width, height, true);
        
        // Draw shell edge
        if (edgeWith > 0) {
        	gc.setLineWidth(edgeWith);
        	Color borderColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
        	gc.setForeground(borderColor); 
        	// TODO: should be edgeWidth / 2 instead of 1?
        	gc.drawRectangle(1, 1, width - edgeWith, height - edgeWith);
        }
        
        gc.dispose();
		return image;
	}
	
	static Image createImage(int width, int height, int color) {
		Display display = Display.getDefault();
		Image image = new Image(display, width, height);
		GC gc = new GC(image);
		gc.setBackground(display.getSystemColor(color));
		gc.fillRectangle(image.getBounds());
		gc.dispose();
		return image;
	}
}
