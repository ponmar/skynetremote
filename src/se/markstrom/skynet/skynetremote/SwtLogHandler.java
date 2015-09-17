package se.markstrom.skynet.skynetremote;

import java.text.DateFormat;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class SwtLogHandler extends Handler {

	private Display display;
	private Text textWidget;
	
	public SwtLogHandler(Display display, Text textWidget) {
		this.display = display;
		this.textWidget = textWidget;
	}
	
	@Override
	public void publish(LogRecord logRecord) {
		String date = DateFormat.getDateInstance(DateFormat.SHORT).format(logRecord.getMillis());
		String time = DateFormat.getTimeInstance().format(logRecord.getMillis());
		
		final String message =  date + " " + time + " " + logRecord.getLevel().toString() + ": " + logRecord.getMessage() + "\n";
		
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				String text = textWidget.getText() + message;
				textWidget.setText(text);
			}
		});
	}

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}
}
