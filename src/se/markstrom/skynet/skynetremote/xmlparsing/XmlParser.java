package se.markstrom.skynet.skynetremote.xmlparsing;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
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

	private Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}
}
