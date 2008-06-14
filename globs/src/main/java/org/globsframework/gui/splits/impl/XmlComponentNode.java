package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitterFactory;
import org.globsframework.saxstack.parser.DefaultXmlNode;
import org.globsframework.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

import java.awt.*;
import java.util.ArrayList;

public class XmlComponentNode extends DefaultXmlNode {
  private String name;
  private SplitterFactory factory;
  private SplitsContext repository;
  private XmlComponentNode parent;
  private java.util.List<Splitter> subSplitters = new ArrayList<Splitter>();
  private Splitter splitter;
  private SplitProperties properties;

  public XmlComponentNode(String name, Attributes attributes, SplitterFactory factory, SplitsContext repository, XmlComponentNode parent) {
    this.name = name;
    this.properties = createProperties(attributes, parent != null ? parent.properties : null);
    this.factory = factory;
    this.repository = repository;
    this.parent = parent;
  }

  private static SplitProperties createProperties(Attributes attributes, SplitProperties parentProperties) {
    DefaultSplitProperties properties = new DefaultSplitProperties(parentProperties);
    for (int i = 0, max = attributes.getLength(); i < max; i++) {
      properties.put(attributes.getLocalName(i), attributes.getValue(i));
    }
    return properties;
  }

  public XmlNode getSubNode(String tag, Attributes attributes) {
    return new XmlComponentNode(tag, attributes, factory, repository, this);
  }

  public void complete() {
    splitter = factory.getSplitter(name,
                                   subSplitters.toArray(new Splitter[subSplitters.size()]),
                                   properties,
                                   repository);
    if (parent != null) {
      parent.subSplitters.add(splitter);
    }
  }

  public Component getComponent() {
    return splitter.getComponentStretch(true).getComponent();
  }
}
