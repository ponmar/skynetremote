package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.markstrom.skynet.skynetremote.model.Log;

public class LogXmlParser extends XmlParser {

	private Log log;
	
	public LogXmlParser(String xml) {
		super(xml);
	}

	public Log getLog() {
		return log;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("entry");
		if (nl != null) {
			String text = "";
			for (int i=0; i<nl.getLength(); i++) {
				Node entryNode = nl.item(i);
				Node entryDataNode = entryNode.getFirstChild();
				if (entryDataNode != null) {
					text += entryDataNode.getNodeValue() + "\n";
				}
			}
			log = new Log(text);
			return true;
		}
		return false;
	}
}
