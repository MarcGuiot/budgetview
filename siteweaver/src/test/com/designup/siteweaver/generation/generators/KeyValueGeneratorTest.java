package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.FileOutput;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

public class KeyValueGeneratorTest extends TestCase {

  private KeyValueGenerator generator;
  protected HtmlOutput output = new FileOutput("tmp/output");

  public void setUp() {
    HtmlTag tag = new HtmlTag();
    tag.addAttribute("key", "name");
    generator = new KeyValueGenerator(tag);
  }

  public void testKeyIsOnGivenTag() throws Exception {
    Page page = new Page("p", "p", "p");
    page.addKeyWithValue("name", "p");
    checkOutput(page, "p");
  }

  public void testKeyIsOnParent() throws Exception {

    Page parent = new Page("p", "p", "p");
    parent.addKeyWithValue("name", "parent_value");
    Page child = new Page("c", "c", "c");
    parent.addSubPage(child);
    checkOutput(child, "");
  }

  private void checkOutput(Page page, String expectedText) throws IOException {
    StringWriter writer = new StringWriter();
    generator.processPage(null, page, new HtmlWriter(writer), output);
    assertEquals(expectedText, writer.toString());
  }
}
