package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SummaryXmlParser extends XmlParser {

	public Integer majorApiVersion;
	public Integer minorApiVersion;
	public String site;
	public Boolean armed;
	public Double countdown;
	public Long latestEventId;
	public Integer numInfoEvents;
	public Integer numMinorEvents;
	public Integer numMajorEvents;
	public Double logTimestamp;
	public String controlChecksum;
	public String weatherChecksum;
	public String time;

	public SummaryXmlParser(String xml) {
		super(xml);
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {

		try {
			majorApiVersion = getNodeValueAsInteger(xmlDoc, "majorapiversion");
			minorApiVersion = getNodeValueAsInteger(xmlDoc, "minorapiversion");
			site = getNodeValueAsString(xmlDoc, "site");
			armed = getNodeValueAsInteger(xmlDoc, "armed") == 1;
			countdown = getNodeValueAsDouble(xmlDoc, "countdown");
			latestEventId = getNodeValueAsLong(xmlDoc, "latestid");
			numInfoEvents = getNodeValueAsInteger(xmlDoc, "info");
			numMinorEvents = getNodeValueAsInteger(xmlDoc, "minor");
			numMajorEvents = getNodeValueAsInteger(xmlDoc, "major");
			logTimestamp = getNodeValueAsDouble(xmlDoc, "logtimestamp");
			controlChecksum = getNodeValueAsString(xmlDoc, "controlchecksum");
			weatherChecksum = getNodeValueAsString(xmlDoc, "weatherchecksum");
			time = getNodeValueAsString(xmlDoc, "time");

			return majorApiVersion != null && minorApiVersion != null && site != null && armed != null && countdown != null && latestEventId != null && numInfoEvents != null && numMinorEvents != null && numMajorEvents != null && logTimestamp != null && controlChecksum != null && weatherChecksum != null && time != null;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
}
