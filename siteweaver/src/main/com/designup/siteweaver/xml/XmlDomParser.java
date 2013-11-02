package com.designup.siteweaver.xml;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class XmlDomParser {

  private XmlDomParser() {
    // no instanciation possible
  }

  public static Element parse(Reader reader, String expectedRootTag) throws XmlParsingException {
    Document document = parse(reader);
    Element root = document.getDocumentElement();
    if (!root.getTagName().equals(expectedRootTag)) {
      throw new InvalidRootTagException(getInvalidRootTagMessage(expectedRootTag, root.getTagName()));
    }
    return root;
  }

  public static class InvalidRootTagException extends XmlParsingException {
    public InvalidRootTagException(String invalidRootTagMessage) {
      super(invalidRootTagMessage);
    }
  }

  private static Document parse(Reader reader) throws XmlParsingException {
    DOMParser parser = new DOMParser();
    try {
      parser.parse(new InputSource(reader));
      reader.close();
      return parser.getDocument();
    }
    catch (IOException e) {
      throw new XmlParsingException("Received IOException: " + e);
    }
    catch (SAXException e) {
      throw new XmlParsingException("Received SAXException: " + e);
    }
  }

  public static NodeList getChildrenWithName(Node node, String name) {
    return new FilteredNodeList(node, name);
  }

  public static String getMandatoryAttribute(Element node, String attributeName)
    throws XmlParsingException {
    String name = node.getAttribute(attributeName);
    if ((name == null) || (name.length() == 0)) {
      String nodeName = node.getNodeName();
      String message = getAttributeNotSetMessage(attributeName, nodeName);
      throw new XmlParsingException(message);
    }
    return name;
  }

  public static String getOptionalAttribute(Element node, String attributeName, String defaultValue) {
    String result = defaultValue;
    if (node.hasAttribute(attributeName)) {
      result = node.getAttribute(attributeName);
    }
    return result;
  }

  public static String getAttributeNotSetMessage(String attributeName,
                                                 String nodeName) {
    return "Mandatory attribute '" + attributeName +
           "' not set in tag '" + nodeName + "'";
  }

  public static String getInvalidRootTagMessage(String expectedTag,
                                                String foundTag) {
    return "Found unexpected XML root tag '" + foundTag + "' instead of '" +
           expectedTag + "'";
  }

  public static int getOptionalIntAttribute(Element node, String attributeName, int defaultValue) {
    int result = defaultValue;
    if (node.hasAttribute(attributeName)) {
      String resultAsString = node.getAttribute(attributeName);
      try {
        result = Integer.parseInt(resultAsString);
      }
      catch (Exception e) {
        result = defaultValue;
      }
    }
    return result;
  }

  public static boolean getOptionalBooleanAttribute(Element node, String attributeName, boolean defaultValue) {
    if (node.hasAttribute(attributeName)) {
      String resultAsString = node.getAttribute(attributeName);
      return Boolean.valueOf(resultAsString);
    }
    return defaultValue;
  }

  private static class FilteredNodeList implements NodeList {
    ArrayList<Node> nodesList = new ArrayList<Node>();

    public FilteredNodeList(Node node, String childName) {
      NodeList allChildren = node.getChildNodes();
      for (int i = 0, size = allChildren.getLength(); i < size; i++) {
        Node child = allChildren.item(i);
        if (child.getNodeName().equals(childName)) {
          nodesList.add(child);
        }
      }
    }

    public int getLength() {
      return nodesList.size();
    }

    public Node item(int i) {
      return nodesList.get(i);
    }
  }
}
