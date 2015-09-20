package se.markstrom.skynet.skynetremote.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogLevelFilter implements Filter {

	private int level;
	
	public LogLevelFilter(Level leastLevel) {
		level = leastLevel.intValue();
	}
	
	@Override
	public boolean isLoggable(LogRecord record) {
		return level <= record.getLevel().intValue();
	}
}
