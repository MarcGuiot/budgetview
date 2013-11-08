package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.model.Page;

public class NextInTourGeneratorTest extends GeneratorTestCase {

  private Generator generator;

  protected Generator getGenerator() {
    return generator;
  }

  public void testPath() throws Exception {
    initGenerator("path");
    checkOutput("", "/p1");
    checkOutput("p1", "/p1-1");
    checkOutput("p1_2_2", "/p1-2-3");
    checkOutput("p1_2_3", "/p1-3");
    checkOutput("p3_3_3", "/root");
  }

  public void testTitle() throws Exception {
    initGenerator("title");
    checkOutput("", "p1");
    checkOutput("p1", "p1_1");
    checkOutput("p1_2_2", "p1_2_3");
    checkOutput("p1_2_3", "p1_3");
    checkOutput("p3_3_3", "root");
  }

  public void testTheUrlIsReturned() throws Exception {
    initGenerator("path");
    Page imagePage = new Page("image.html", "title", "shortTitle");
    checkOutput(imagePage, "/image");
  }

  public void testDefaultOutputTypeIsPath() throws Exception {
    initGenerator(null);
    checkOutput("", "/p1");
  }

  private void initGenerator(String contentType) {
    HtmlTag tag = new HtmlTag();
    if (contentType != null) {
      tag.addAttribute("output", contentType);
    }
    generator = new NextInTourGenerator(tag);
  }
}
