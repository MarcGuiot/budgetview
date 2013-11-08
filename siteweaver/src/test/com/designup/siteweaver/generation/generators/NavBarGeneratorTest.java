package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;

public class NavBarGeneratorTest extends GeneratorTestCase {

  protected Generator getGenerator() {
    return new NavBarGenerator(formatter);
  }

  public void testAll() throws Exception {
    checkOutput("", "[*#(root),(p1),(p2),(p3)]");
    checkOutput("p1", "[#(root),*#(p1),(p2),(p3)]");
    checkOutput("p2", "[#(root),(p1),*#(p2),(p3)]");
    checkOutput("p2_2", "[#(root),(p1),#(p2),(p3)]");
  }
}
