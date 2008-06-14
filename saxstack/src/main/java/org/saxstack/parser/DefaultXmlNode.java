package org.saxstack.parser;

import org.xml.sax.Attributes;

public class DefaultXmlNode implements org.saxstack.parser.XmlNode {

  public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
    return SilentXmlNode.INSTANCE;
  }

  public void setValue(String value) {
  }

  /**
   * Default implementation does nothing.
   */
  public void complete() {
  }

}
