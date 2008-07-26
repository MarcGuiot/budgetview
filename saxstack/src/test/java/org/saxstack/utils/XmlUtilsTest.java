package org.saxstack.utils;

import junit.framework.TestCase;
import org.saxstack.writer.PathFilter;
import org.xml.sax.helpers.AttributesImpl;

public class XmlUtilsTest extends TestCase {
  public static final String ATTR_NAME = "attr";

  public void test() throws Exception {
    String input =
      "<A>" +
      "  <B a='a' b='b'>" +
      "    <C/>" +
      "  </B>" +
      "  <C/>" +
      "</A>";

    String s = XmlUtils.format(input, XmlUtils.getXmlReader(), 4, new PathFilter("A/B"));
    assertEquals("<A>\n" +
                 "  <B a=\"a\" b=\"b\"/>\n" +
                 "</A>", s);
    s = XmlUtils.format(input, XmlUtils.getXmlReader(), 4, new PathFilter("A/B/*"));
    assertEquals("<A>\n" +
                 "  <B a=\"a\" b=\"b\">\n" +
                 "    <C/>\n" +
                 "  </B>\n" +
                 "</A>", s);
  }

  public void testGetBooleanAttrValue() throws Exception {
    checkGetBooleanAttrValue("false", false, false);
    checkGetBooleanAttrValue("true", false, true);
    checkGetBooleanAttrValue("false", true, false);
    checkGetBooleanAttrValue("true", true, true);
    checkGetBooleanAttrValue(null, true, true);
    checkGetBooleanAttrValue(null, false, false);
    checkGetBooleanAttrValue("xxx", false, false);
    checkGetBooleanAttrValue("xxx", true, false);
  }

  public void checkGetBooleanAttrValue(String value, boolean defaultValue, boolean expected) throws Exception {
    AttributesImpl attributes = createAttributes(ATTR_NAME, value);
    assertEquals(expected, XmlUtils.getBooleanAttrValue(ATTR_NAME, attributes, defaultValue));
  }

  private AttributesImpl createAttributes(String attr, String value) {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", attr, attr, "", value);
    return attributes;
  }

  public void testGetIntAttrValue() throws Exception {
    checkGetIntAttrValue("12", 0, 12);
    checkGetIntAttrValue(null, 99, 99);
    try {
      checkGetIntAttrValue("xxx", 0, 0);
      fail();
    }
    catch (NumberFormatException e) {
    }
  }

  public void checkGetIntAttrValue(String value, int defaultValue, int expected) throws Exception {
    AttributesImpl attributes = createAttributes(ATTR_NAME, value);
    assertEquals(expected, XmlUtils.getIntAttrValue(ATTR_NAME, attributes, defaultValue));
  }

  public void testGetDoubleAttrValue() throws Exception {
    checkGetDoubleAttrValue("12.3", 0, 12.3);
    checkGetDoubleAttrValue(null, 99.9, 99.9);
    try {
      checkGetDoubleAttrValue("xxx", 0, 0);
      fail();
    }
    catch (NumberFormatException e) {
    }
  }

  public void checkGetDoubleAttrValue(String value, double defaultValue, double expected) throws Exception {
    AttributesImpl attributes = createAttributes(ATTR_NAME, value);
    assertEquals(expected, XmlUtils.getDoubleAttrValue(ATTR_NAME, attributes, defaultValue), 0);
  }
}
