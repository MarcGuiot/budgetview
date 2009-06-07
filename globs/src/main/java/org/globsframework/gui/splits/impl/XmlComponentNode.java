package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitterFactory;
import org.globsframework.gui.splits.styles.SplitsPath;
import org.globsframework.gui.splits.xml.SplitsParser;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.XmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;

import java.awt.*;
import java.util.ArrayList;

public class XmlComponentNode extends DefaultXmlNode {
  private String name;
  private SplitterFactory factory;
  private SplitsContext context;
  private XmlComponentNode parent;
  private java.util.List<Splitter> subSplitters = new ArrayList<Splitter>();
  private Splitter splitter;
  private SplitProperties properties;
  private SplitsPath path;

  public XmlComponentNode(String name, Attributes attributes, SplitterFactory factory,
                          SplitsContext context, XmlComponentNode parent) {
    this.name = name;
    this.factory = factory;
    this.context = context;
    this.parent = parent;
    this.path = new SplitsPath(parent != null ? parent.path : null, name,
                               getName(attributes), getClass(attributes));
    this.properties = getProperties(attributes, parent);
  }

  private SplitProperties getProperties(Attributes attributes, XmlComponentNode parent) {
    DefaultSplitProperties properties = new DefaultSplitProperties(parent != null ? parent.properties : null);
    properties.add(this.context.getStyles().getProperties(path));
    properties.add(SplitsParser.createProperties(attributes));
    return properties;
  }

  public XmlNode getSubNode(String tag, Attributes attributes) {
    return new XmlComponentNode(tag, attributes, factory, context, this);
  }

  private String getName(Attributes attributes) {
    String name = XmlUtils.getAttrValue("name", attributes, null);
    if (name != null) {
      return name;
    }
    return XmlUtils.getAttrValue("ref", attributes, null);
  }

  private String getClass(Attributes attributes) {
    return XmlUtils.getAttrValue("styleClass", attributes, null);
  }

  public void complete() {
    splitter = factory.getSplitter(name,
                                   subSplitters.toArray(new Splitter[subSplitters.size()]),
                                   properties,
                                   context);
    if (parent != null) {
      parent.subSplitters.add(splitter);
    }
  }

  public Component getComponent() {
    return splitter.createComponentStretch(context, true).getComponent();
  }
}
