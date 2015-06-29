package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SummaryXmlParser extends XmlParser {

	private int majorApiVersion;
	private int minorApiVersion;
	private String site;
	private boolean armed;
	private double countdown;
	private long latestEventId;
	private int numInfoEvents;
	private int numMinorEvents;
	private int numMajorEvents;
	private double logTimestamp;
	private String time;

	public SummaryXmlParser(String xml) {
		super(xml);
	}

	public long getLatestEventId() {
		return latestEventId;
	}
	
	public double getLogTimestamp() {
		return logTimestamp;
	}
	
	public boolean getArmedState() {
		return armed;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		String value = getNodeValueAsString(xmlDoc, "majorapiversion");
		if (value == null) {
			return false;
		}
		majorApiVersion = Integer.parseInt(value);

		value = getNodeValueAsString(xmlDoc, "minorapiversion");
		if (value == null) {
			return false;
		}
		minorApiVersion = Integer.parseInt(value);

		site = getNodeValueAsString(xmlDoc, "site");
		if (site == null) {
			return false;
		}
		
		value = getNodeValueAsString(xmlDoc, "armed");
		if (value == null) {
			return false;
		}
		armed = Integer.parseInt(value) == 1;

		value = getNodeValueAsString(xmlDoc, "countdown");
		if (value == null) {
			return false;
		}
		countdown = Double.parseDouble(value);

		value = getNodeValueAsString(xmlDoc, "latestid");
		if (value == null) {
			return false;
		}
		latestEventId = Long.parseLong(value);

		value = getNodeValueAsString(xmlDoc, "info");
		if (value == null) {
			return false;
		}
		numInfoEvents = Integer.parseInt(value);

		value = getNodeValueAsString(xmlDoc, "minor");
		if (value == null) {
			return false;
		}
		numMinorEvents = Integer.parseInt(value);
		
		value = getNodeValueAsString(xmlDoc, "major");
		if (value == null) {
			return false;
		}
		numMajorEvents = Integer.parseInt(value);

		value = getNodeValueAsString(xmlDoc, "logtimestamp");
		if (value == null) {
			return false;
		}
		logTimestamp = Double.parseDouble(value);
		
		time = getNodeValueAsString(xmlDoc, "time");
		if (time == null) {
			return false;
		}

		return true;
	}
}
