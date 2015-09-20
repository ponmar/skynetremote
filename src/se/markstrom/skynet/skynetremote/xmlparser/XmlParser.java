package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

abstract class XmlParser {
	
	private static final Logger log = Logger.getLogger(XmlParser.class.getName());
	static {
		log.setLevel(Level.ALL);
	}
	
	private boolean isValid;
	
	XmlParser(File filename) {
		this(readFile(filename));
	}
	
	XmlParser(String xml) {
		try {
			if (xml != null) {
				log.finer("Pre parsing XML document");
				Document xmlDoc = loadXMLFromString(xml);
				log.finer("Post parsing XML document");
				
				log.finer("Pre loading XML document data");
				isValid = parse(xmlDoc);
				log.finer("Post loading XML document data with result: " + isValid);
			}
			else {
				log.finer("No XML data available");
				isValid = false;
			}
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			log.finer("Exception while parsing XML data");
			isValid = false;
		}
	}

	public boolean isValid() {
		return isValid;
	}
	
	/**
	 * Implement this method in XML parsing subclass.
	 * @param xmlDoc
	 * @return parse result
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	abstract protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException;

	protected String getNodeValueAsString(Document xmlDoc, String tagName) {
		NodeList nl = xmlDoc.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() == 1) {
			Node n = nl.item(0);
			Node firstChild = n.getFirstChild();
			if (firstChild != null) {
				return firstChild.getNodeValue();
			}
			else {
				return "";
			}
		}
		return null;
	}
	
	protected Integer getNodeValueAsInteger(Document xmlDoc, String tagName) {
		String value = getNodeValueAsString(xmlDoc, tagName);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			}
			catch (NumberFormatException e) {
			}
		}
		return null;
	}
	
	protected Boolean getNodeValueAsBoolean(Document xmlDoc, String tagName) {
		String value = getNodeValueAsString(xmlDoc, tagName);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			}
			catch (NumberFormatException e) {
			}
		}
		return null;
	}
	
	private Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}
	
	private static String readFile(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			String separator = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null ) {
				stringBuilder.append(line);
				stringBuilder.append(separator);
			}

			String content = stringBuilder.toString();
			reader.close();
			return content;
		}
		catch (IOException e) {
			return null;
		}
	}
}
