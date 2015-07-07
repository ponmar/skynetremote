package se.markstrom.skynet.skynetremote.xmlwriter;

import se.markstrom.skynet.skynetremote.Settings;

public class SettingsXmlWriter extends XmlWriter {
	
	private Settings settings;
	
	public SettingsXmlWriter(Settings settings) {
		super(Settings.FILENAME);
		this.settings = settings;
	}
	
	@Override
	protected String createXml() {
		String xml = XML_HEADER + "<settings>";
		xml += "<getNewEvents>" + settings.getNewEvents + "</getNewEvents>";
		xml += "<getNewControl>" + settings.getNewControl + "</getNewControl>";
		xml += "<getNewLog>" + settings.getNewLog + "</getNewLog>";
		xml += "<pollSummary>" + settings.pollSummary + "</pollSummary>";
		xml += "<summaryPollInterval>" + settings.summaryPollInterval + "</summaryPollInterval>";
		xml += "<host>" + settings.host + "</host>";
		xml += "<port>" + settings.port + "</port>";
		xml += "<showEventNotification>" + settings.notifyOnNewEvent + "</showEventNotification>";
		xml += "</settings>";
		return xml;
	}
}
