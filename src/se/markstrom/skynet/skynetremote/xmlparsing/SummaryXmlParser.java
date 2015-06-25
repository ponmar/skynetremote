package se.markstrom.skynet.skynetremote.xmlparsing;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SummaryXmlParser extends XmlParser {

	private double logTimestamp;

	public SummaryXmlParser(String xml) {
		super(xml);
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		// Parse log timestamp
		NodeList nl = xmlDoc.getElementsByTagName("logtimestamp");
		if (nl != null && nl.getLength() == 1) {
			Node n = nl.item(0);
			Node firstChild = n.getFirstChild();
			logTimestamp = Double.parseDouble(firstChild.getNodeValue());
		}
		else {
			return false;
		}
		
		return true;
	}

	public double getLogTimestamp() {
		return logTimestamp;
	}
}
