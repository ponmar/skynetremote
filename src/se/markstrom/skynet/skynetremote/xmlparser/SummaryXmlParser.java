package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.Summary;

public class SummaryXmlParser extends XmlParser {

	private Summary summary;

	public SummaryXmlParser(String xml) {
		super(xml);
	}

	public Summary getSummary() {
		return summary;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {

		Integer majorApiVersion;
		Integer minorApiVersion;
		String site;
		Boolean armed;
		Double countdown;
		Long latestEventId;
		Integer numInfoEvents;
		Integer numMinorEvents;
		Integer numMajorEvents;
		Double logTimestamp;
		String controlChecksum;
		String time;
		
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
			time = getNodeValueAsString(xmlDoc, "time");

			if (majorApiVersion == null || minorApiVersion == null || site == null || armed == null || countdown == null || latestEventId == null || numInfoEvents == null || numMinorEvents == null || numMajorEvents == null || logTimestamp == null || controlChecksum == null || time == null) {
				return false;
			}
			
			summary = new Summary(majorApiVersion, minorApiVersion, site, armed, countdown, latestEventId, numInfoEvents, numMinorEvents, numMajorEvents, logTimestamp, controlChecksum, time);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
}
