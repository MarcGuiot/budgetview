package org.crossbowlabs.saxstack.utils;

import junit.framework.TestCase;
import org.apache.xerces.parsers.SAXParser;
import org.crossbowlabs.saxstack.parser.SaxStackParser;
import org.crossbowlabs.saxstack.writer.PrettyPrintRootXmlTag;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlNodeToBuilderTest extends TestCase {

  public void testReadWrite() throws Exception {
    checkFormat("<root><A attr1='val'/><B><C>some values</C></B></root>",
                "<root>\n" +
                "  <A attr1=\"val\"/>\n" +
                "  <B>\n" +
                "    <C>\n" +
                "      some values\n" +
                "    </C>\n" +
                "  </B>\n" +
                "</root>");
  }

  public void testChechAttributeOrder() throws Exception {
    checkFormat("<root><A bttr='val2' attr1='val'/></root>",
                "<root>\n" +
                "  <A attr1=\"val\" bttr=\"val2\"/>\n" +
                "</root>");

  }

  private void checkFormat(String input, String output) throws IOException {

    StringWriter actual = new StringWriter();
    SaxStackParser.parse(new SAXParser(), new XmlNodeToBuilder(new PrettyPrintRootXmlTag(actual, 4), null),
                         new StringReader(input));
    assertEquals(output,
                 actual.toString());
  }

}
