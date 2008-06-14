package org.globsframework.saxstack.parser;

import org.xml.sax.Attributes;

public interface XmlNode {

  public XmlNode getSubNode(String childName, Attributes xmlAttrs) throws ExceptionHolder;

  public void setValue(String value) throws ExceptionHolder;

  public void complete() throws ExceptionHolder;
}
