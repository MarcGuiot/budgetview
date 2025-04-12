package com.designup.siteweaver.xml;

import junit.framework.TestCase;
import org.w3c.dom.Element;

import java.io.StringReader;

public class XmlDomParserTest extends TestCase {

  public void testNominalCase() throws Exception {
    String stream = "<toto attr=\"1\"/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertEquals("toto", root.getTagName());
    assertEquals("1", XmlDomParser.getMandatoryAttribute(root, "attr"));
  }

  public void testBadRootTag() throws Exception {
    String stream = "<toto attr=\"1\"/>";
    try {
      XmlDomParser.parse(new StringReader(stream), "unknownTag");
      fail();
    }
    catch (XmlParsingException e) {
      assertEquals(XmlDomParser.getInvalidRootTagMessage("unknownTag", "toto"),
                   e.getMessage());
    }
  }

  public void testMandatoryAttributeNotSet() throws Exception {
    String stream = "<toto attr=\"1\"/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    try {
      XmlDomParser.getMandatoryAttribute(root, "unknownAttribute");
    }
    catch (XmlParsingException e) {
      assertEquals(XmlDomParser.getAttributeNotSetMessage("unknownAttribute", "toto"),
                   e.getMessage());
    }
  }

  public void testGetOptionalIntParameter() throws Exception {
    String stream = "<toto attr=\"1\"/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertEquals(1, XmlDomParser.getOptionalIntAttribute(root, "attr", 2));
  }

  public void testGetOptionalIntParameterWithDefaultValue() throws Exception {
    String stream = "<toto attr=\"\"/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertEquals(2, XmlDomParser.getOptionalIntAttribute(root, "attr", 2));
  }

  public void testGetOptionalIntParameterWithMissingAttribute() throws Exception {
    String stream = "<toto/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertEquals(2, XmlDomParser.getOptionalIntAttribute(root, "attr", 2));
  }

  public void testGetOptionalBooleanParameter() throws Exception {
    String stream = "<toto attr=\"true\"/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertTrue(XmlDomParser.getOptionalBooleanAttribute(root, "attr", false));
  }

  public void testGetOptionalBoleanParameterWithDefaultValue() throws Exception {
    String stream = "<toto attr=\"\"/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertTrue(!XmlDomParser.getOptionalBooleanAttribute(root, "attr", false));
  }

  public void testGetOptionalBooleanParameterWithMissingAttribute() throws Exception {
    String stream = "<toto/>";
    Element root = XmlDomParser.parse(new StringReader(stream), "toto");
    assertTrue(!XmlDomParser.getOptionalBooleanAttribute(root, "attr", false));
  }
}