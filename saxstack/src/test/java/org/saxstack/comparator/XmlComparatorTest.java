package org.saxstack.comparator;

import junit.framework.TestCase;
import org.saxstack.utils.XmlUtils;

import java.io.StringReader;

public class XmlComparatorTest extends TestCase {

  public void testNoDiff() throws Exception {
    String xml = "<hello/>";
    checkXml(xml, xml, true, true);
  }

  public void testDifferent() throws Exception {
    String xml1 = "<hello/>";
    String xml2 = "<bye/>";
    checkXml(xml1, xml2, false, false);
  }

  public void testChildrenInDifferentOrderAreEquivalentButNotEquals() throws Exception {
    String xml1 = "<hello><tag1/><tag2/><tag3/></hello>";
    String xml2 = "<hello><tag3/><tag1/><tag2/></hello>";
    checkXml(xml1, xml2, true, false);
  }

  public void testAttributeDifference() throws Exception {
    String xml1 = "<hello><tag1/><tag2 a=\"A\"/><tag3/></hello>";
    String xml2 = "<hello><tag3/><tag1/><tag2/></hello>";
    checkXml(xml1, xml2, false, false);
  }

  public void testAttributeInDifferentOrder() throws Exception {
    String xml1 = "<hello><tag1 a=\"A\" b=\"B\"/></hello>";
    String xml2 = "<hello><tag1 b=\"B\" a=\"A\"/></hello>";
    checkXml(xml1, xml2, true, true);
  }

  public void testAgainDifferentOrders() throws Exception {
    String xml1 =
      "<struct>" +
      "  <val1>0</val1>" +
      "  <val2>Paris-Tokyo</val2>" +
      "  <val1>1</val1>" +
      "</struct>";
    String xml2 =
      "<struct>" +
      "  <val2>Paris-Tokyo</val2>" +
      "  <val1>0</val1>" +
      "  <val1>1</val1>" +
      "</struct>";

    checkXml(xml1, xml2, true, false);
  }

  public void testWithSpacesAndCarriageReturns() throws Exception {
    String xml1 =
      "<struct>" +
      "  <val1    toto='5151'   >  0    \n   </val1>" +
      "  \n    <val2>Paris-Tokyo           </val2>" +
      "</struct>";

    String xml2 = "<struct><val1 toto='5151'>0</val1><val2>Paris-Tokyo</val2></struct>";

    checkXml(xml1, xml2, true, true);
  }

  public void testIdenticalTagCountAreTakenIntoAccount() throws Exception {
    String xml1 =
      "<root>" +
      "  <child/>" +
      "  <child/>" +
      "</root>";
    String xml2 =
      "<root>" +
      "  <child/>" +
      "  <child/>" +
      "  <child/>" +
      "</root>";
    checkXml(xml1, xml2, false, false);
  }

  public void testSameCharacters() throws Exception {
    String xml1 = "<hello><tag1>XYZ</tag1></hello>";
    String xml2 = "<hello><tag1>XYZ</tag1></hello>";
    checkXml(xml1, xml2, true, true);
  }

  public void testWhitespace() throws Exception {
    String xml1 =
      "<hello>" +
      "  <list>" +
      "    <tag1>XYZ</tag1>" +
      "    <tag2>PQR</tag2>" +
      "    <tag3>" +
      "      <tag4/>" +
      "    </tag3>" +
      "  </list>" +
      "</hello>";
    String xml2 = "<hello><list><tag1>XYZ</tag1><tag2>PQR</tag2><tag3><tag4/></tag3></list></hello>";
    checkXml(xml1, xml2, true, true);
    checkXml(xml2, xml1, true, true);
  }

  public void testNotSameCharacters() throws Exception {
    String xml1 = "<hello><tag1>ABC</tag1></hello>";
    String xml2 = "<hello><tag1>XYZ</tag1></hello>";
    checkXml(xml1, xml2, false, false);
  }

  public void testSame() throws Exception {
    String xml1 = "<hello></hello>";
    String xml2 = "<hello/>";
    checkXml(xml1, xml2, true, true);
  }

  public void testIsSubset() throws Exception {
    String expected = "<A a='a'/>";
    String actual = "<A a='a' b='b'/>";
    checkIsSubset(expected, actual);
  }

  public void testIsSubsetInSub() throws Exception {
    String expected = "<A a='a'>" +
                      "  <B d='e'/>" +
                      "  <B d='d'/>" +
                      "</A>";
    String actual = "<A a='a' b='b'>" +
                    "  <B c='c' d='d'/>" +
                    "  <B c='c' d='e'/>" +
                    "</A>";
    checkIsSubset(expected, actual);
  }

  public void testIsSubsetFailsIfMissingAttribute() throws Exception {
    String expected = "<A a='a'>" +
                      "  <B d='e'/>" +
                      "  <B d='d'/>" +
                      "</A>";
    String actual = "<A a='a' b='b'>" +
                    "  <B c='c'/>" +
                    "  <B c='c' d='e'/>" +
                    "</A>";
    checkNotASubset(expected,
                    actual,
                    "<A a='a'>" +
                    "  <B/>" +
                    "  <B d='e'/>" +
                    "</A>");
  }

  public void testIsSubsetFailIfWithDifferentNumberOfAttributes() throws Exception {
    String expected = "<A a='a'>" +
                      "  <B d='d'/>" +
                      "  <B d='d' e='e'/>" +
                      "</A>";
    String actual = "<A a='a' b='b'>" +
                    "  <B d='d'/>" +
                    "  <B d='d' e='e'/>" +
                    "</A>";
    checkIsSubset(expected, actual);
  }

  public void testIsSubsetFailsIfMissingXmlTag() throws Exception {
    String expected = "<A a='a'>" +
                      "  <B d='e'/>" +
                      "  <B d='d'/>" +
                      "</A>";
    String actual = "<A a='a' b='b'>" +
                    "  <B c='c' d='e'/>" +
                    "</A>";
    checkNotASubset(expected,
                    actual,
                    "<A a='a'>" +
                    "  <B d='e'/>" +
                    "</A>");
  }

  public void testIsSubsetFailsIfExtraNewTag() throws Exception {
    String expected = "<A>" +
                      "  <B d='e'/>" +
                      "  <B d='d'/>" +
                      "</A>";
    String actual = "<A>" +
                    "  <B d='e'/>" +
                    "  <B d='d'/>" +
                    "  <C c='c'/>" +
                    "</A>";
    checkNotASubset(expected,
                    actual,
                    "<A>" +
                    "  <B d='e'/>" +
                    "  <B d='d'/>" +
                    "  <C c='c'/>" +
                    "</A>");
  }

  public void testIsSubsetFailsIfMissingNewTag() throws Exception {
    String expected = "<A a='a'>" +
                      "  <B d='e'/>" +
                      "  <B d='d'/>" +
                      "  <C c='c'/>" +
                      "</A>";
    String actual = "<A a='a' b='b'>" +
                    "  <B d='e'/>" +
                    "  <B d='d'/>" +
                    "</A>";
    checkNotASubset(expected,
                    actual,
                    "<A a='a'>" +
                    "  <B d='e'/>" +
                    "  <B d='d'/>" +
                    "</A>");
  }

  private void checkXml(String xmlA, String xmlB, boolean isEquivalent, boolean isEqual) throws Exception {
    if (isEqual) {
      assertTrue(isEquivalent);
      checkAssertEquivalentIsOkForStringAndReader(xmlA, xmlB);
      checkAssertEqualIsOkForStringAndReader(xmlA, xmlB);
    }
    else {
      if (!isEquivalent) {
        checkAssertEquivalentFailsForStringAndReader(xmlA, xmlB);
      }
      else {
        checkAssertEquivalentIsOkForStringAndReader(xmlA, xmlB);
      }
      checkAssertEqualsFailsForStringAndReader(xmlA, xmlB);
    }
  }

  private void checkAssertEquivalentFailsForStringAndReader(String xmlA, String xmlB) throws Exception {
    assertFalse(XmlComparator.areEquivalent(xmlA, xmlB, XmlUtils.getXmlReader()));
  }

  private void checkAssertEqualsFailsForStringAndReader(String xmlA, String xmlB) throws Exception {
    assertFalse(XmlComparator.areEqual(xmlA, xmlB, XmlUtils.getXmlReader()));
  }

  private void checkAssertEquivalentIsOkForStringAndReader(String xmlA, String xmlB) throws Exception {
    assertTrue(XmlComparator.areEquivalent(xmlA, xmlB, XmlUtils.getXmlReader()));
    assertTrue(XmlComparator.areEquivalent(new StringReader(xmlA),
                                           new StringReader(xmlB), XmlUtils.getXmlReader()));
  }

  private void checkAssertEqualIsOkForStringAndReader(String xmlA, String xmlB) throws Exception {
    assertTrue(XmlComparator.areEqual(xmlA, xmlB, XmlUtils.getXmlReader()));
    assertTrue(XmlComparator.areEqual(new StringReader(xmlA),
                                      new StringReader(xmlB),
                                      XmlUtils.getXmlReader()));
  }

  private void checkIsSubset(String expected, String actual) throws Exception {
    assertTrue(XmlComparator.computeDiff(expected, actual, XmlUtils.getXmlReader()) == null);
  }

  private void checkNotASubset(String expected, String actual, String filteredActual) throws Exception {
    XmlComparator.Diff subset =
      XmlComparator.computeDiff(expected, actual, XmlUtils.getXmlReader());
    assertNotNull(subset);
    if (!XmlComparator.areEqual(filteredActual,
                                subset.getFilteredActual(), XmlUtils.getXmlReader())) {
      assertEquals(filteredActual, subset.getFilteredActual());
      fail();
    }
  }
}
