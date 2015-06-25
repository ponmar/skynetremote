package se.markstrom.skynet.skynetremote.xmlparsing;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LogXmlParser extends XmlParser {

	private String log;
	
	public LogXmlParser(String xml) {
		super(xml);
	}

	public String getLogText() {
		return log;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("entry");
		if (nl != null) {
			log = "";
			for (int i=0; i<nl.getLength(); i++) {
				Node entryNode = nl.item(i);
				Node entryDataNode = entryNode.getFirstChild();
				if (entryDataNode != null) {
					log += entryDataNode.getNodeValue() + "\n";
				}
			}
			return true;
		}
		return false;
	}
}
