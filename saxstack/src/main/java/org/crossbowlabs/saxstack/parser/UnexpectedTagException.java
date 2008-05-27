package org.crossbowlabs.saxstack.parser;

public class UnexpectedTagException extends XmlParsingException {
  public UnexpectedTagException(String parent, String child) {
    super("Unexpected tag " + child + " found under " + parent);
  }
}
