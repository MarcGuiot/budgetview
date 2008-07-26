package org.saxstack.parser;

import junit.framework.TestCase;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SaxStackParserTest extends TestCase {

  public void testParsingWithNamespace() throws Exception {
    DummyXmlNode node = new DummyXmlNode();
    SaxStackParser.parse(XmlUtils.getXmlReader(), node,
                         new StringReader(
                           "<root xmlns:ptf='aa'>" +
                           "  <ptf:item>Baby food</ptf:item>" +
                           "</root>"));
    assertEquals(1, node.children.size());
    assertEquals("item", ((DummyXmlNode)node.children.get(0)).name);
  }

  static class DummyXmlNode implements XmlNode {
    String name;
    List children = new ArrayList();
    public String value;

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
      name = childName;
      DummyXmlNode child = new DummyXmlNode();
      children.add(child);
      return child;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public void complete() {
    }

    public DummyXmlNode get(int i) {
      return (DummyXmlNode)children.get(0);
    }
  }

  public void testNewLineInValueShouldNotBeIgnored() throws Exception {
    DummyXmlNode node = new DummyXmlNode();
    SaxStackParser.parse(XmlUtils.getXmlReader(), node,
                         new StringReader(
                           "<root xmlns:ptf='aa'>" +
                           "  <ptf:item>Baby\n" +
                           "food</ptf:item>" +
                           "</root>"));
    DummyXmlNode childNode = node.get(0);
    assertEquals("Baby\nfood", childNode.get(0).value);
  }

  public void testWithThreeLevels() throws Exception {
    DummyXmlNode node = new DummyXmlNode();
    SaxStackParser.parse(XmlUtils.getXmlReader(), node,
                         new StringReader(
                           "<root >" +
                           "  <item>" +
                           "    <item>Baby food</item>" +
                           "  </item>" +
                           "</root>"));
    DummyXmlNode childNode = node.get(0);
    childNode = childNode.get(0);
    assertEquals("Baby food", childNode.get(0).value);
  }

  public void testWithDTD() throws Exception {
    DummyXmlNode node = new DummyXmlNode();
    SaxStackParser.parse(XmlUtils.getXmlReader(), node,
                         new StringReader(
                           "<?xml version='1.0' encoding='UTF-8'?>" +
                           "<!DOCTYPE root [" +
                           "  <!ELEMENT root (root*,item*)>" +
                           "  <!ATTLIST root name CDATA #IMPLIED>" +
                           "  <!ATTLIST root otherName CDATA #IMPLIED>" +
                           "]>" +
                           "<root name='zzz'>" +
                           "  <item q='a'>" +
                           "    <other/>" +
                           "  </item>" +
                           "</root>"));
    DummyXmlNode childNode = node.get(0);
    childNode = childNode.get(0);
    assertEquals("other", childNode.name);
  }

  public void testWithExternalDTD() throws Exception {
    DummyXmlNode node = new DummyXmlNode();
    final InputStream inputStream =
      new ByteArrayInputStream(new StringBuffer()
        .append("<?xml version='1.0' encoding='UTF-8'?>")
        .append("<!ELEMENT root (root*,item*)>")
        .append("<!ATTLIST root name CDATA #IMPLIED>")
        .append("<!ATTLIST root otherName CDATA #IMPLIED>")
        .toString().getBytes());
    SaxStackParser.parse(XmlUtils.getXmlReader(), node,
                         new StringReader(
                           "<?xml version='1.0' encoding='UTF-8'?>" +
                           "<!DOCTYPE test SYSTEM 'file:test.dtd' >" +
                           "<root name='zzz'>" +
                           "  <item q='a'>" +
                           "    <other/>" +
                           "  </item>" +
                           "</root>"),
                         new EntityResolver() {
                           public InputSource resolveEntity(String publicId, String systemId) {
                             return new InputSource(inputStream);
                           }
                         });
    DummyXmlNode childNode = node.get(0);
    childNode = childNode.get(0);
    assertEquals("other", childNode.name);
  }

}