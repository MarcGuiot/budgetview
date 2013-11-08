package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;
import com.designup.siteweaver.html.HtmlTag;

import java.io.IOException;

public class MenuGeneratorTest extends GeneratorTestCase {

  protected Generator getGenerator() {
    return new MenuGenerator(formatter);
  }

  public void test() throws IOException {
    checkOutput("", "[*#(root),(p1),(p2),(p3)]");
    checkOutput("p1", "[#(root),*#(p1),(p1_1),(p1_2),(p1_3),(p2),(p3)]");
    checkOutput("p2", "[#(root),(p1),*#(p2),(p2_1),(p2_2),(p2_3),(p3)]");
    checkOutput("p2_2",
                "[#(root),(p1),#(p2),(p2_1),*#(p2_2),(p2_2_1),(p2_2_2),(p2_2_3),(p2_3),(p3)]");
  }
}
