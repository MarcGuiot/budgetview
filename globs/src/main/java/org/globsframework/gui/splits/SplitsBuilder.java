package org.globsframework.gui.splits;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.impl.XmlComponentNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.SaxStackParser;
import org.saxstack.parser.XmlNode;
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
  private static final SAXParser PARSER = new SAXParser();

  public static SplitsBuilder init(Directory directory) {
    return new SplitsBuilder(directory.get(ColorService.class),
                             directory.find(IconLocator.class),
                             directory.find(TextLocator.class),
                             directory.find(FontLocator.class));
  }
  
  public static SplitsBuilder init(ColorService colorService, IconLocator locator) {
    return new SplitsBuilder(colorService, locator);
  }

  public SplitsBuilder(ColorService colorService, IconLocator locator) {
    this(colorService, locator, null, null);
  }

  public SplitsBuilder(ColorService colorService, IconLocator iconLocator, TextLocator textLocator, FontLocator fontLocator) {
    if (iconLocator == null) {
      iconLocator = IconLocator.NULL;
    }
    if (textLocator == null) {
      textLocator = TextLocator.NULL;
    }
    this.context = new DefaultSplitsContext(colorService, iconLocator, textLocator, fontLocator);
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
    synchronized (PARSER) {
      SplitsBootstrapXmlNode bootstrap = new SplitsBootstrapXmlNode();
      SaxStackParser.parse(PARSER, bootstrap, reader);
      return bootstrap.getRootComponent();
    }
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
      XmlComponentNode node = new XmlComponentNode(tag, attributes, factory, context, null);
      root = node;
      return node;
    }
  }

}
