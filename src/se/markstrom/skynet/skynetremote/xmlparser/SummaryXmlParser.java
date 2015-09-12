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
		
		summary = new Summary();
		
		String value = getNodeValueAsString(xmlDoc, "majorapiversion");
		if (value == null) {
			return false;
		}
		summary.majorApiVersion = Integer.parseInt(value);

		value = getNodeValueAsString(xmlDoc, "minorapiversion");
		if (value == null) {
			return false;
		}
		summary.minorApiVersion = Integer.parseInt(value);

		summary.site = getNodeValueAsString(xmlDoc, "site");
		if (summary.site == null) {
			return false;
		}
		
		value = getNodeValueAsString(xmlDoc, "armed");
		if (value == null) {
			return false;
		}
		summary.armed = Integer.parseInt(value) == 1;

		value = getNodeValueAsString(xmlDoc, "countdown");
		if (value == null) {
			return false;
		}
		summary.countdown = Double.parseDouble(value);

		value = getNodeValueAsString(xmlDoc, "latestid");
		if (value == null) {
			return false;
		}
		summary.latestEventId = Long.parseLong(value);

		value = getNodeValueAsString(xmlDoc, "info");
		if (value == null) {
			return false;
		}
		summary.numInfoEvents = Integer.parseInt(value);

		value = getNodeValueAsString(xmlDoc, "minor");
		if (value == null) {
			return false;
		}
		summary.numMinorEvents = Integer.parseInt(value);
		
		value = getNodeValueAsString(xmlDoc, "major");
		if (value == null) {
			return false;
		}
		summary.numMajorEvents = Integer.parseInt(value);

		value = getNodeValueAsString(xmlDoc, "logtimestamp");
		if (value == null) {
			return false;
		}
		summary.logTimestamp = Double.parseDouble(value);
		
		summary.controlChecksum = getNodeValueAsString(xmlDoc, "controlchecksum");
		if (summary.controlChecksum == null) {
			return false;
		}
		
		summary.time = getNodeValueAsString(xmlDoc, "time");
		if (summary.time == null) {
			return false;
		}

		return true;
	}
}
