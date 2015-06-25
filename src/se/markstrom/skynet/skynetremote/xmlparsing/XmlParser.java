package se.markstrom.skynet.skynetremote.xmlparsing;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

abstract class XmlParser {
	
	private boolean isValid = true;
	
	XmlParser(String xml) {
		try {
			xml = xml.substring(0, xml.length()-3);
			System.out.println("Parsing xml:\n" + xml);
			Document xmlDoc = loadXMLFromString(xml);
			isValid = parse(xmlDoc);
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			isValid = false;
		}
	}

	public boolean isValid() {
		return isValid;
	}
	
	abstract protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException;

	protected String getUniqueNodeText(Document xmlDoc, String tagName) {
		NodeList nl = xmlDoc.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() == 1) {
			Node n = nl.item(0);
			Node firstChild = n.getFirstChild();
			return firstChild.getNodeValue();
		}
		return null;
	}
	
	private Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}
}
