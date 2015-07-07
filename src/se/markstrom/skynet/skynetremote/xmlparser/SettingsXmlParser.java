package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.Settings;

public class SettingsXmlParser extends XmlParser {
	
	private Settings settings;
	
	public SettingsXmlParser() {
		super(new File(Settings.FILENAME));
	}

	public Settings getSettings() {
		return settings;
	}
	
	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		Boolean getNewEvents = getNodeValueAsBoolean(xmlDoc, "getNewEvents");
		Boolean getNewControl = getNodeValueAsBoolean(xmlDoc, "getNewControl");
		Boolean getNewLog = getNodeValueAsBoolean(xmlDoc, "getNewLog");
		Boolean pollSummary = getNodeValueAsBoolean(xmlDoc, "pollSummary");
		Integer summaryPollInterval = getNodeValueAsInteger(xmlDoc, "summaryPollInterval");
		String host = getNodeValueAsString(xmlDoc, "host");
		Integer port = getNodeValueAsInteger(xmlDoc, "port");
		Boolean showEventNotification = getNodeValueAsBoolean(xmlDoc, "showEventNotification");
		
		if (getNewEvents != null && getNewControl != null && getNewLog != null &&
				pollSummary != null && summaryPollInterval != null &&
				port != null && showEventNotification != null) {
			settings = new Settings();
			settings.getNewEvents = getNewEvents;
			settings.getNewControl = getNewControl;
			settings.getNewLog = getNewLog;
			settings.pollSummary = pollSummary;
			settings.summaryPollInterval = summaryPollInterval;
			settings.host = host;
			settings.port = port;
			settings.notifyOnNewEvent = showEventNotification;
			return settings.validate();
		}
		
		return false;
	}
}
