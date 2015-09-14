package se.markstrom.skynet.skynetremote.xmlwriter;

import se.markstrom.skynet.api.SkynetAPI.Protocol;
import se.markstrom.skynet.skynetremote.Settings;

public class SettingsXmlWriter extends XmlWriter {
	
	private Settings settings;
	
	public SettingsXmlWriter(Settings settings) {
		super(Settings.FILENAME);
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
		xml += "<summaryPollInterval>" + settings.summaryPollInterval + "</summaryPollInterval>";
		xml += "<host>" + settings.host + "</host>";
		xml += "<port>" + settings.port + "</port>";
		xml += "<protocol>" + protocol + "</protocol>";
		xml += "<showEventNotification>" + settings.notifyOnNewEvent + "</showEventNotification>";
		xml += "</settings>";
		return xml;
	}
}
