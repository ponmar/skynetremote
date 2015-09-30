package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.skynetremote.model.Settings;

public class SettingsXmlParser extends XmlParser {
	
	private Settings settings;
	
	public SettingsXmlParser(String filename) {
		super(new File(filename));
	}

	public Settings getSettings() {
		return settings;
	}
	
	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		Boolean getNewEvents = getNodeValueAsBoolean(xmlDoc, "getNewEvents");
		Boolean getNewControl = getNodeValueAsBoolean(xmlDoc, "getNewControl");
		Boolean getNewLog = getNodeValueAsBoolean(xmlDoc, "getNewLog");
		Boolean getNewWeather = getNodeValueAsBoolean(xmlDoc, "getNewWeather");
		Integer summaryPollInterval = getNodeValueAsInteger(xmlDoc, "summaryPollInterval");
		String host = getNodeValueAsString(xmlDoc, "host");
		Integer port = getNodeValueAsInteger(xmlDoc, "port");
		Integer protocol = getNodeValueAsInteger(xmlDoc, "protocol");
		Boolean showInfoEventNotification = getNodeValueAsBoolean(xmlDoc, "showInfoEventNotification");
		Boolean showMinorEventNotification = getNodeValueAsBoolean(xmlDoc, "showMinorEventNotification");
		Boolean showMajorEventNotification = getNodeValueAsBoolean(xmlDoc, "showMajorEventNotification");
		Boolean logDetails = getNodeValueAsBoolean(xmlDoc, "logDetails");
		
		if (getNewEvents != null && getNewControl != null && getNewLog != null &&
				summaryPollInterval != null && port != null && protocol != null &&
				getNewWeather != null &&
				showInfoEventNotification != null &&
				showMinorEventNotification != null &&
				showMajorEventNotification != null &&
				logDetails != null) {
			settings = new Settings();
			settings.getNewEvents = getNewEvents;
			settings.getNewControl = getNewControl;
			settings.getNewLog = getNewLog;
			settings.getNewWeather = getNewWeather;
			settings.summaryPollInterval = summaryPollInterval;
			settings.host = host;
			settings.port = port;
			if (protocol == 0) {
				settings.protocol = Protocol.SSH;
			}
			else {
				settings.protocol = Protocol.TELNET;
			}
			settings.notifyOnNewInfoEvent = showInfoEventNotification;
			settings.notifyOnNewMinorEvent = showMinorEventNotification;
			settings.notifyOnNewMajorEvent = showMajorEventNotification;
			settings.logDetails = logDetails;
			return settings.validate();
		}
		
		return false;
	}
}
