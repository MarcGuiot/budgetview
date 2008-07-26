package org.saxstack.utils;

import junit.framework.TestCase;
import org.saxstack.parser.SaxStackParser;
import org.saxstack.writer.PrettyPrintRootXmlTag;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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

  private void checkFormat(String input, String output) throws IOException, SAXException, ParserConfigurationException {

    StringWriter actual = new StringWriter();
    SaxStackParser.parse(XmlUtils.getXmlReader(), new XmlNodeToBuilder(new PrettyPrintRootXmlTag(actual, 4), null),
                         new StringReader(input));
    assertEquals(output,
                 actual.toString());
  }

}
