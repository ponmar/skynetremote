package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	
	XmlParser(File filename) {
		this(readFile(filename));
	}
	
	XmlParser(String xml) {
		try {
			//System.out.println("Pre parsing xml doc");
			Document xmlDoc = loadXMLFromString(xml);
			//System.out.println("Post parsing xml doc");
			
			//System.out.println("Pre loading xml doc data");
			isValid = parse(xmlDoc);
			//System.out.println("Post loading xml doc data with result: " + isValid);
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
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
			StringBuilder  stringBuilder = new StringBuilder();
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
			return "";
		}
	}
}
