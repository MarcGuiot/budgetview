package org.crossbowlabs.splits;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.impl.DefaultSplitsContext;
import org.crossbowlabs.splits.impl.XmlComponentNode;
import org.crossbowlabs.splits.layout.CardHandler;
import org.crossbowlabs.splits.layout.DefaultCardHandler;
import org.crossbowlabs.splits.splitters.DefaultSplitterFactory;
import org.crossbowlabs.saxstack.parser.DefaultXmlNode;
import org.crossbowlabs.saxstack.parser.SaxStackParser;
import org.crossbowlabs.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class SplitsBuilder {

  private DefaultSplitsContext context;
  private SplitterFactory factory = new DefaultSplitterFactory();

  public static SplitsBuilder init(ColorService colorService, IconLocator locator) {
    return new SplitsBuilder(colorService, locator);
  }

  public SplitsBuilder(ColorService colorService) {
    this(colorService, null, null);
  }

  public SplitsBuilder(ColorService colorService, IconLocator locator) {
    this(colorService, locator, null);
  }

  public SplitsBuilder(ColorService colorService, IconLocator iconLocator, TextLocator textLocator) {
    if (iconLocator == null) {
      iconLocator = IconLocator.NULL;
    }
    if (textLocator == null) {
      textLocator = TextLocator.NULL;
    }
    this.context = new DefaultSplitsContext(colorService, iconLocator, textLocator);
  }

  public SplitsBuilder add(String name, Component component) {
    component.setName(name);
    context.addComponent(name, component);
    return this;
  }

  public SplitsBuilder add(Component... components) {
    for (Component component : components) {
      String name = component.getName();
      if (name == null) {
        throw new SplitsException("Component '" + component + "' must have a name");
      }
      add(name, component);
    }
    return this;
  }

  public SplitsBuilder add(String name, Action action) {
    context.add(name, action);
    return this;
  }

  public CardHandler addCardHandler(String handlerName) {
    JPanel panel = new JPanel();
    add(handlerName, panel);
    return DefaultCardHandler.init(panel);
  }

  public Component parse(Class referenceClass, String resourceName) {
    InputStream stream = referenceClass.getResourceAsStream(resourceName);
    if (stream == null) {
      throw new SplitsException("File '" + resourceName + "' not found in classpath");
    }
    try {
      return parse(stream);
    }
    catch (Exception e) {
      throw new SplitsException("Error parsing file '" + resourceName + "' - " + e.getMessage(), e);
    }
  }

  public Component parse(InputStream inputStream) {
    if (inputStream == null) {
      throw new IllegalArgumentException("null inputStream");
    }
    return parse(new InputStreamReader(inputStream));
  }

  public Component parse(InputStream inputStream, String encoding) throws UnsupportedEncodingException {
    return parse(new InputStreamReader(inputStream, encoding));
  }

  public Component parse(Reader reader) throws SplitsException {
    SplitsBootstrapXmlNode bootstrap = new SplitsBootstrapXmlNode();
    SaxStackParser.parse(new SAXParser(), bootstrap, reader);
    return bootstrap.getRootComponent();
  }

  public Component getComponent(String id) {
    return context.findComponent(id);
  }

  private class SplitsBootstrapXmlNode extends DefaultXmlNode {
    private SplitsXmlNode splitsNode = new SplitsXmlNode();

    public SplitsBootstrapXmlNode() {
    }

    public XmlNode getSubNode(String tag, Attributes attributes) {
      if (!tag.equals("splits")) {
        throw new SplitsException("The root element of a Splits XML file must be <splits>");
      }
      return splitsNode;
    }

    public Component getRootComponent() {
      return splitsNode.root.getComponent();
    }
  }

  private class SplitsXmlNode extends DefaultXmlNode {
    private XmlComponentNode root;

    public SplitsXmlNode() {
    }

    public XmlNode getSubNode(String tag, Attributes attributes) {
      XmlComponentNode node = new XmlComponentNode(tag, attributes, factory, context, null);
      root = node;
      return node;
    }
  }

}
