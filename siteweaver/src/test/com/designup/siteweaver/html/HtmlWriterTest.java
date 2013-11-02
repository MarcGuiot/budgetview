package com.designup.siteweaver.html;

import junit.framework.TestCase;

import java.io.StringWriter;

public class HtmlWriterTest extends TestCase {
  private HtmlWriter writer;
  private StringWriter stringWriter;

  protected void setUp() throws Exception {
    stringWriter = new StringWriter();
    writer = new HtmlWriter(stringWriter);
  }

  public void testLink() throws Exception {
    writer.writeLink("link", "url");
    checkString("<a href=\"url\">link</a>");
  }

  private void checkString(String s) {
    assertEquals(s, stringWriter.toString());
  }
}
