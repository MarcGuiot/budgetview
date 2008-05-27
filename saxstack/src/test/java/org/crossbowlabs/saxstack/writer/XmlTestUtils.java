package org.crossbowlabs.saxstack.writer;

import junit.framework.Assert;
import org.apache.xerces.parsers.SAXParser;
import org.crossbowlabs.saxstack.comparator.XmlComparator;
import org.crossbowlabs.saxstack.utils.XmlUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class XmlTestUtils {
  public static void assertEquivalent(String expectedXml, String resultXml) throws Exception {
    SAXParser parser = new SAXParser();
    if (!XmlComparator.areEquivalent(expectedXml, resultXml, parser)) {
      Assert.assertEquals(XmlUtils.format(expectedXml, parser, 4), XmlUtils.format(resultXml, parser, 4));
    }
  }

  public static void assertEquivalent(Reader xmlA, Reader xmlB) throws Exception {
    assertEquivalent(readerToString(xmlA), readerToString(xmlB));
  }

  public static void assertEquals(String xmlA, String xmlB) throws Exception {
    SAXParser parser = new SAXParser();
    if (!XmlComparator.areEqual(xmlA, xmlB, parser)) {
      Assert.assertEquals(XmlUtils.format(xmlA, parser, 4), XmlUtils.format(xmlB, parser, 4));
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
