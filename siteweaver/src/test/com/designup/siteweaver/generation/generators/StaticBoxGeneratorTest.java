package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;

import java.io.File;

public class StaticBoxGeneratorTest extends GeneratorTestCase {
  protected Generator getGenerator() {
    return new StaticBoxGenerator();
  }

  public void testEmptyGeneration() throws Exception {
    checkOutput("", "");
  }

  public void testOneBox() throws Exception {
    rootPage.addBorderBox("dir/box.html");
    File file = dumpHtmlToFile("tmp/input/dir/box.html", "Box content");
    checkOutput("", "Box content");
    file.delete();
  }
}
