package se.markstrom.skynet.skynetremote;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.eclipse.swt.widgets.Display;

public class SwtLogHandler extends Handler {

	private Display display;
	
	public SwtLogHandler(Display display) {
		this.display = display;
	}
	
	@Override
	public void publish(LogRecord logRecord) {
		Formatter f = getFormatter();
		if (f != null) {
			final String message = f.format(logRecord);
			
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					// TODO: add log record to GUI text widget
				}
			});
		}
	}

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}
}
