package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;

public class PathGeneratorTest extends GeneratorTestCase {

  protected Generator getGenerator() {
    return new PathGenerator(formatter);
  }

  public void testAll() throws Exception {
    checkOutput("", "[*#(root)]");
    checkOutput("p1", "[#(root),*#(p1)]");
    checkOutput("p1_2_3", "[#(root),#(p1),#(p1_2),*#(p1_2_3)]");
  }
}
