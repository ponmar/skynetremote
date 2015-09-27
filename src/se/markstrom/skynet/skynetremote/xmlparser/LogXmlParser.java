package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LogXmlParser extends XmlParser {

	private ArrayList<String> logItems;
	
	public LogXmlParser(String xml) {
		super(xml);
	}

	public ArrayList<String> getLogItems() {
		return logItems;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("entry");
		if (nl != null) {
			logItems = new ArrayList<String>();
			for (int i=0; i<nl.getLength(); i++) {
				Node entryNode = nl.item(i);
				Node entryDataNode = entryNode.getFirstChild();
				if (entryDataNode != null) {
					logItems.add(entryDataNode.getNodeValue());
				}
			}
			return true;
		}
		return false;
	}
}
