package org.globsframework.gui.splits.xml;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.SplitterFactory;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;
import org.globsframework.gui.splits.impl.XmlComponentNode;
import org.globsframework.utils.exceptions.InvalidData;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.SaxStackParser;
import org.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

import java.awt.*;
import java.io.Reader;
import java.util.Arrays;

public class SplitsParser {

  private SplitsContext context;
  private SplitterFactory factory;
  private static final SAXParser PARSER = new SAXParser();

  public SplitsParser(SplitsContext context, SplitterFactory factory) {
    this.context = context;
    this.factory = factory;
  }

  public Component parse(Reader reader) {
    synchronized (PARSER) {
      SplitsBootstrapXmlNode bootstrap = new SplitsBootstrapXmlNode();
      SaxStackParser.parse(PARSER, bootstrap, reader);
      return bootstrap.getRootComponent();
    }
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

    public Component getRootComponent() {
      XmlComponentNode root = splitsNode.root;
      if (root == null) {
        throw new InvalidData("Empty file");
      }
      return root.getComponent();
    }
  }

  private class SplitsXmlNode extends DefaultXmlNode {
    private XmlComponentNode root;

    public SplitsXmlNode() {
    }

    public XmlNode getSubNode(String tag, Attributes attributes) {
      if (tag.equals("styles")) {
        return new StylesXmlNode(context);
      }

      XmlComponentNode node = new XmlComponentNode(tag, attributes, factory, context, null);
      root = node;
      return node;
    }
  }
}
