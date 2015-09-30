package se.markstrom.skynet.skynetremote.xmlwriter;

import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.skynetremote.model.Settings;

public class SettingsXmlWriter extends XmlWriter {
	
	private Settings settings;
	
	public SettingsXmlWriter(Settings settings, String filename) {
		super(filename);
		this.settings = settings;
	}
	
	@Override
	protected String createXml() {
		int protocol;
		if (settings.protocol == Protocol.SSH) {
			protocol = 0;
		}
		else {
			protocol = 1;
		}
		
		String xml = XML_HEADER + "<settings>";
		xml += "<getNewEvents>" + settings.getNewEvents + "</getNewEvents>";
		xml += "<getNewControl>" + settings.getNewControl + "</getNewControl>";
		xml += "<getNewSensors>" + settings.getNewSensors + "</getNewSensors>";
		xml += "<getNewLog>" + settings.getNewLog + "</getNewLog>";
		xml += "<getNewWeather>" + settings.getNewWeather + "</getNewWeather>";
		xml += "<summaryPollInterval>" + settings.summaryPollInterval + "</summaryPollInterval>";
		xml += "<host>" + settings.host + "</host>";
		xml += "<port>" + settings.port + "</port>";
		xml += "<protocol>" + protocol + "</protocol>";
		xml += "<showInfoEventNotification>" + settings.notifyOnNewInfoEvent + "</showInfoEventNotification>";
		xml += "<showMinorEventNotification>" + settings.notifyOnNewMinorEvent + "</showMinorEventNotification>";
		xml += "<showMajorEventNotification>" + settings.notifyOnNewMajorEvent + "</showMajorEventNotification>";
		xml += "<logDetails>" + settings.logDetails + "</logDetails>";
		xml += "</settings>";
		return xml;
	}
}
