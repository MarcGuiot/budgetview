package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class BookMenuGeneratorTest extends GeneratorTestCase {

  protected Generator getGenerator() {
    return new BookMenuGenerator(new DummyMenuGenerator());
  }

  public void testMenuOnlyShownForTaggedRoots() throws Exception {
    getPage("p1_2").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    checkOutput("", "");
    checkOutput("p2_1", "");
    checkOutput("p2_2", "");
  }

  public void testShownForRoot() throws Exception {
    getPage("").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    checkOutput("",
                "[\n" +
                "root*\n" +
                "p1\n" +
                "p2\n" +
                "p3\n" +
                "]\n");
  }

  public void testOnlyOneLevelBelowActiveIsExpanded() throws Exception {
    getPage("").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    checkOutput("p2",
                "[\n" +
                "root\n" +
                "p1\n" +
                "p2*\n" +
                "  [\n" +
                "  p2_1\n" +
                "  p2_2\n" +
                "  p2_3\n" +
                "  ]\n" +
                "p3\n" +
                "]\n");
  }

  public void testLocalTree() throws Exception {
    getPage("p2_1").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    checkOutput("p2_1",
                "[\n" +
                "p2_1*\n" +
                "p2_1_1\n" +
                "p2_1_2\n" +
                "p2_1_3\n" +
                "]\n");
  }

  public void testLocalTreeWithSelectedLeaf() throws Exception {
    getPage("p2_1").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    checkOutput("p2_1_2",
                "[\n" +
                "p2_1\n" +
                "p2_1_1\n" +
                "p2_1_2*\n" +
                "p2_1_3\n" +
                "]\n");
  }

  private class DummyMenuGenerator implements BookMenuGenerator.Formatter {
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
