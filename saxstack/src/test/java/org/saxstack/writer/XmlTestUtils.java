package org.saxstack.writer;

import junit.framework.Assert;
import org.saxstack.comparator.XmlComparator;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class XmlTestUtils {

  public static void assertEquivalent(String expectedXml, String resultXml) throws Exception {
    XMLReader reader = XmlUtils.getXmlReader();
    if (!XmlComparator.areEquivalent(expectedXml, resultXml, reader)) {
      Assert.assertEquals(XmlUtils.format(expectedXml, reader, 4),
                          XmlUtils.format(resultXml, reader, 4));
    }
  }

  public static void assertEquivalent(Reader xmlA, Reader xmlB) throws Exception {
    assertEquivalent(readerToString(xmlA), readerToString(xmlB));
  }

  public static void assertEquals(String xmlA, String xmlB) throws Exception {
    XMLReader reader = XmlUtils.getXmlReader();
    if (!XmlComparator.areEqual(xmlA, xmlB, reader)) {
      Assert.assertEquals(XmlUtils.format(xmlA, reader, 4),
                          XmlUtils.format(xmlB, reader, 4));
    }
  }

  public static void assertEquals(Reader xmlA, Reader xmlB) throws Exception {
    assertEquals(readerToString(xmlA), readerToString(xmlB));
  }

  static String readerToString(Reader reader) throws IOException {
    StringBuffer stringBuffer = new StringBuffer();
    BufferedReader bufferedReaderA = new BufferedReader(reader);
    String readingString = bufferedReaderA.readLine();
    while (readingString != null) {
      stringBuffer.append(readingString);
      readingString = bufferedReaderA.readLine();
    }
    return stringBuffer.toString();
  }
}
