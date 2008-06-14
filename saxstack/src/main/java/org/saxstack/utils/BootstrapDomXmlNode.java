package org.saxstack.utils;

import org.saxstack.parser.ExceptionHolder;
import org.saxstack.parser.XmlNode;
import org.xml.sax.Attributes;

public class BootstrapDomXmlNode implements XmlNode {
  private DomXmlNode child;

  public XmlNode getSubNode(String childName, Attributes xmlAttrs) throws ExceptionHolder {
    if (child != null) {
      throw new RuntimeException("Only one child is allowed");
    }
    child = new DomXmlNode(childName, xmlAttrs);
    return child;
  }

  public void setValue(String value) throws ExceptionHolder {
    throw new RuntimeException("setValue not allowed on bootstrap node");
  }

  public void complete() throws ExceptionHolder {
  }

  public DomXmlNode getChild() {
    return child;
  }

  public boolean contains(BootstrapDomXmlNode otherRoot) {
    return child.contains(otherRoot.child);
  }
}
