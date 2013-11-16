package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class BookTOCGeneratorTest extends GeneratorTestCase {

  private Generator generator;

  protected Generator getGenerator() {
    return generator;
  }

  public void testRootWithOneLevel() throws Exception {
    init("root.html", 1);
    checkOutput("",
                "(\n" +
                "  [\n" +
                "  p1\n" +
                "  p2\n" +
                "  p3\n" +
                "  ]\n" +
                ")\n");
  }

  public void testRootWith2Levels() throws Exception {
    init(null, 2);
    checkOutput("",
                "(\n" +
                "  [\n" +
                "  p1\n" +
                "    [\n" +
                "    p1_1\n" +
                "    p1_2\n" +
                "    p1_3\n" +
                "    ]\n" +
                "  p2\n" +
                "    [\n" +
                "    p2_1\n" +
                "    p2_2\n" +
                "    p2_3\n" +
                "    ]\n" +
                "  p3\n" +
                "    [\n" +
                "    p3_1\n" +
                "    p3_2\n" +
                "    p3_3\n" +
                "    ]\n" +
                "  ]\n" +
                ")\n");
  }

  public void testStartsFromCurrentPageIfNoRootGiven() throws Exception {
    init(null, 1);
    checkOutput("",
                "(\n" +
                "  [\n" +
                "  p1\n" +
                "  p2\n" +
                "  p3\n" +
                "  ]\n" +
                ")\n");

    checkOutput("p2",
                "(\n" +
                "  [\n" +
                "  p2_1\n" +
                "  p2_2\n" +
                "  p2_3\n" +
                "  ]\n" +
                ")\n");
  }

  public void testCanUsePathFromAnotherPartOfTheTree() throws Exception {
    init("p1.html", 1);
    checkOutput("p2",
                "(\n" +
                "  [\n" +
                "  p1_1\n" +
                "  p1_2\n" +
                "  p1_3\n" +
                "  ]\n" +
                ")\n");
  }

  public void testCanUsePathFromSubtree() throws Exception {
    init("p2.html", 1);
    checkOutput("",
                "(\n" +
                "  [\n" +
                "  p2_1\n" +
                "  p2_2\n" +
                "  p2_3\n" +
                "  ]\n" +
                ")\n");
  }

  private void init(String rootPath, int depth) {
    HtmlTag tag = new HtmlTag();
    if (rootPath != null) {
      tag.addAttribute("root", rootPath);
    }
    tag.addAttribute("depth", Integer.toString(depth));
    generator = new BookTOCGenerator(tag, new DummyTOCGenerator());
  }

  private class DummyTOCGenerator implements BookTOCGenerator.Formatter {

    public void writeMenuStart(Page menuRootPage, HtmlWriter writer) {
      writer.write("(\n");
    }

    public void writeMenuEnd(HtmlWriter writer) {
      writer.write(")\n");
    }

    public void writeStart(HtmlWriter writer, int depth) {
      writePadding(depth, writer);
      writer.write("[\n");
    }

    public void writeElement(Page page, int depth, boolean active, HtmlWriter writer) {
      writePadding(depth, writer);
      writer.write(page.getTitle());
      if (active) {
        writer.write('*');
      }
      writer.write('\n');
    }

    private void writePadding(int depth, HtmlWriter writer) {
      for (int i = 0; i < depth * 2; i++) {
        writer.write(' ');
      }
    }

    public void writeEnd(HtmlWriter writer, int depth) {
      writePadding(depth, writer);
      writer.write("]\n");
    }
  }
}
