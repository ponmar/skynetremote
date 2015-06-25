package se.markstrom.skynet.skynetremote.xmlparsing;

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
	private int latestEventId;
	private int numInfoEvents;
	private int numMinorEvents;
	private int numMajorEvents;
	private double logTimestamp;
	private String time;

	public SummaryXmlParser(String xml) {
		super(xml);
	}

	public int getLatestEventId() {
		return latestEventId;
	}
	
	public double getLogTimestamp() {
		return logTimestamp;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		String value = getUniqueNodeText(xmlDoc, "majorapiversion");
		if (value == null) {
			return false;
		}
		majorApiVersion = Integer.parseInt(value);

		value = getUniqueNodeText(xmlDoc, "minorapiversion");
		if (value == null) {
			return false;
		}
		minorApiVersion = Integer.parseInt(value);

		site = getUniqueNodeText(xmlDoc, "site");
		if (site == null) {
			return false;
		}
		
		value = getUniqueNodeText(xmlDoc, "armed");
		if (value == null) {
			return false;
		}
		armed = Integer.parseInt(value) == 1;

		value = getUniqueNodeText(xmlDoc, "countdown");
		if (value == null) {
			return false;
		}
		countdown = Double.parseDouble(value);

		value = getUniqueNodeText(xmlDoc, "latestid");
		if (value == null) {
			return false;
		}
		latestEventId = Integer.parseInt(value);

		value = getUniqueNodeText(xmlDoc, "info");
		if (value == null) {
			return false;
		}
		numInfoEvents = Integer.parseInt(value);

		value = getUniqueNodeText(xmlDoc, "minor");
		if (value == null) {
			return false;
		}
		numMinorEvents = Integer.parseInt(value);
		
		value = getUniqueNodeText(xmlDoc, "major");
		if (value == null) {
			return false;
		}
		numMajorEvents = Integer.parseInt(value);

		value = getUniqueNodeText(xmlDoc, "logtimestamp");
		if (value == null) {
			return false;
		}
		logTimestamp = Double.parseDouble(value);
		
		time = getUniqueNodeText(xmlDoc, "time");
		if (time == null) {
			return false;
		}

		return true;
	}
}
