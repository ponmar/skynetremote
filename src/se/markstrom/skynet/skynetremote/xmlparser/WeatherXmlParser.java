package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.WeatherReport;

public class WeatherXmlParser extends XmlParser {
	
	private ArrayList<WeatherReport> weatherReports;
	
	public WeatherXmlParser(String xml) {
		super(xml);
	}

	public ArrayList<WeatherReport> getWeatherReports() {
		return weatherReports; 
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("report");
		if (nl != null) {
			weatherReports = new ArrayList<WeatherReport>();
			for (int i=0; i<nl.getLength(); i++) {
				Node deviceNode = nl.item(i);
				NamedNodeMap attributes = deviceNode.getAttributes();
				
				String provider = attributes.getNamedItem("provider").getFirstChild().getNodeValue();
				String updated = attributes.getNamedItem("updated").getFirstChild().getNodeValue();
				String validFrom = attributes.getNamedItem("validfrom").getFirstChild().getNodeValue();
				String validTo = attributes.getNamedItem("validto").getFirstChild().getNodeValue();
				String area = attributes.getNamedItem("area").getFirstChild().getNodeValue();
				String sunrise = attributes.getNamedItem("sunrise").getFirstChild().getNodeValue();
				String sunset = attributes.getNamedItem("sunset").getFirstChild().getNodeValue();
				String windCode = attributes.getNamedItem("windcode").getFirstChild().getNodeValue();
				
				Double temperature = null;
				String temperatureStr = attributes.getNamedItem("temperature").getFirstChild().getNodeValue();
				if (!temperatureStr.equals("")) {
					temperature = Double.parseDouble(temperatureStr);
				}
				
				Double humidity = null;
				String humidityStr = attributes.getNamedItem("humidity").getFirstChild().getNodeValue();
				if (!humidityStr.equals("")) {
					humidity = Double.parseDouble(humidityStr);
				}
				
				Double precipitation = null;
				String precipitationStr = attributes.getNamedItem("precipitation").getFirstChild().getNodeValue();
				if (!precipitationStr.equals("")){
					precipitation = Double.parseDouble(precipitationStr);
				}
				
				Double windspeed = null;
				String windspeedStr = attributes.getNamedItem("windspeed").getFirstChild().getNodeValue(); 
				if (!windspeedStr.equals("")) {
					windspeed = Double.parseDouble(windspeedStr);
				}
				
				Double pressure = null;
				String pressureStr = attributes.getNamedItem("pressure").getFirstChild().getNodeValue();
				if (!pressureStr.equals("")) {
					pressure = Double.parseDouble(pressureStr);
				}
				
				WeatherReport report = new WeatherReport(provider, updated, validFrom, validTo, area, sunrise, sunset, windCode, temperature, humidity, precipitation, windspeed, pressure);
				weatherReports.add(report);
			}
			return true;
		}
		return false;
	}
}
