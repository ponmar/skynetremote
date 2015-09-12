package se.markstrom.skynet.skynetremote.xmlparser;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CamerasXmlParser extends XmlParser {
	
	private ArrayList<Integer> cameraIndexes;
	
	public CamerasXmlParser(String xml) {
		super(xml);
	}
	
	public ArrayList<Integer> getCameraIndexes() {
		return cameraIndexes;
	}

	@Override
	protected boolean parse(Document xmlDoc) throws ParserConfigurationException, SAXException, IOException {
		NodeList nl = xmlDoc.getElementsByTagName("index");
		if (nl != null) {
			cameraIndexes = new ArrayList<Integer>();
			for (int i=0; i<nl.getLength(); i++) {
				Node indexNode = nl.item(i);
				Node indexDataNode = indexNode.getFirstChild();
				cameraIndexes.add(Integer.parseInt(indexDataNode.getNodeValue()));
			}
			return true;
		}
		return false;
	}
}
