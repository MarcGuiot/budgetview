package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;

import java.io.File;

public class ContentGeneratorTest extends GeneratorTestCase {
  protected Generator getGenerator() {
    return new ContentGenerator();
  }

  public void test() throws Exception {
    File file = dumpHtmlToFile("tmp/input/root.html", "Blah blah blah");
    checkOutput("", "Blah blah blah");
    file.delete();
  }
}
