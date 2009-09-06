package org.globsframework.gui.splits.xml;

import org.globsframework.gui.splits.*;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;
import org.globsframework.gui.splits.impl.XmlComponentNode;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.ExceptionHolder;
import org.saxstack.parser.SaxStackParser;
import org.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.Reader;
import java.util.Arrays;

public class SplitsParser {

  private SplitsContext context;
  private SplitterFactory factory;
  private static SAXParserFactory xmlFactory = SAXParserFactory.newInstance();

  static {
    xmlFactory.setNamespaceAware(true);
  }

  public SplitsParser(SplitsContext context, SplitterFactory factory) {
    this.context = context;
    this.factory = factory;
  }

  public Splitter.SplitComponent parse(Reader reader) {

    SplitsBootstrapXmlNode bootstrap = new SplitsBootstrapXmlNode();
    try {
      javax.xml.parsers.SAXParser PARSER = xmlFactory.newSAXParser();
      SaxStackParser.parse(PARSER.getXMLReader(), bootstrap, reader);
    }
    catch (SAXException e) {
      throw new ExceptionHolder(e);
    }
    catch (ParserConfigurationException e) {
      throw new ExceptionHolder(e);
    }
    return bootstrap.getRootComponent();
  }

  public static SplitProperties createProperties(Attributes attributes,
                                                 String... propertiesToExclude) {
    Arrays.sort(propertiesToExclude);
    DefaultSplitProperties properties = new DefaultSplitProperties();
    for (int i = 0, max = attributes.getLength(); i < max; i++) {
      String name = attributes.getLocalName(i);
      if (Arrays.binarySearch(propertiesToExclude, name) < 0) {
        properties.put(name, attributes.getValue(i));
      }
    }
    return properties;
  }

  private class SplitsBootstrapXmlNode extends DefaultXmlNode {
    private SplitsXmlNode splitsNode;

    public SplitsBootstrapXmlNode() {
      splitsNode = new SplitsXmlNode();
    }

    public XmlNode getSubNode(String tag, Attributes attributes) {
      if (!tag.equals("splits")) {
        throw new SplitsException("The root element of a Splits XML file must be <splits>");
      }
      return splitsNode;
    }

    public Splitter.SplitComponent getRootComponent() {
      XmlComponentNode root = splitsNode.root;
      return root != null ? root.getComponent() : null;
    }
  }

  private class SplitsXmlNode extends DefaultXmlNode {
    private XmlComponentNode root;

    public SplitsXmlNode() {
    }

    public XmlNode getSubNode(String tag, Attributes attributes) {
      if (tag.equals("styleImport")) {
        return new StyleImportNode(attributes, context);
      }
      if (tag.equals("styles")) {
        return new StylesXmlNode(context);
      }

      XmlComponentNode node = new XmlComponentNode(tag, attributes, factory, context, null);
      root = node;
      return node;
    }
  }
}
