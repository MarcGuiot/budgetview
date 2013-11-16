package com.designup.siteweaver.html;

import junit.framework.TestCase;

import java.io.StringReader;
import java.io.StringWriter;

public class HtmlParserTest extends TestCase {

  public void testBasicCase() {
    HtmlTag tag = checkParsing(
      "Salut<GEN NAME=\"toto\" ATTR1 =\n \"value 1\" \n > ... et le reste",
      "gen",
      "Salut");
    assertTrue(tag.getTagName().equals("gen"));
    assertTrue(tag.containsAttribute("attr1"));
    assertTrue(tag.getAttributeValue("attr1").equals("value 1"));
  }

  public void testEarlyEnd() {
    checkParsingError("Salut<GEN NAME=\"toto\" ATTR1 = \"value", "gen");
  }

  public void testInvalidName() {

    String testString = "Salut<titi NAME = \"toto\" > ";
    StringReader reader = new StringReader(testString);
    HtmlParser parser = new HtmlParser(reader);
    StringWriter writer = new StringWriter();
    HtmlTag tag = parser.findNextTag("gen", writer);
    assertTrue(writer.toString().equals(testString));

    assertNull(tag);
  }

  public void testMissingEqualSign() {
    String testString = "Salut<gen NAME \"toto\" > ";
    StringReader reader = new StringReader(testString);
    HtmlParser parser = new HtmlParser(reader);
    StringWriter writer = new StringWriter();
    HtmlTag tag = parser.findNextTag("gen", writer);
    assertNull(tag);
    assertTrue(writer.toString().equals(testString));
  }

  public void testMissingOpeningQuote() {
    checkParsingError("Salut<titi NAME = toto\" > ", "titi");
  }

  public void testMissingClosingQuote() {
    checkParsingError("Salut<gen NAME  =  \"toto > a e i ", "gen");
  }

  private HtmlTag checkParsing(String htmlStream, String tagName, String result) {
    HtmlParser parser = new HtmlParser(new StringReader(htmlStream));
    StringWriter writer = new StringWriter();
    HtmlTag tag = parser.findNextTag(tagName, writer);
    assertTrue(writer.toString().equals(result));
    return tag;
  }

  private void checkParsingError(String htmlStream, String tagName) {
    HtmlParser parser = new HtmlParser(new StringReader(htmlStream));
    StringWriter writer = new StringWriter();
    HtmlTag tag = parser.findNextTag(tagName, writer);
    assertTrue(writer.toString().equals(htmlStream));
    assertNull(tag);
  }
}
