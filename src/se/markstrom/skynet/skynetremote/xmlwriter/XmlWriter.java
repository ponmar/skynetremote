package se.markstrom.skynet.skynetremote.xmlwriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

abstract class XmlWriter {

	private String filename;
	protected static final String XML_HEADER = "<?xml version=\"1.0\"?>";
	
	public XmlWriter(String filename) {
		this.filename = filename;
	}
	
	public void write() {
		writeStringToFile(createXml(), filename);
	}
	
	/**
	 * Implement in XML writing subclass.
	 * @return
	 */
	protected abstract String createXml();
	
	private void writeStringToFile(String data, String filename) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filename));
			writer.write(data);
		}
		catch (IOException e) {
		}
		finally	{
			try	{
				if ( writer != null)
					writer.close( );
			}
			catch ( IOException e) {
			}
		}
	}
}
