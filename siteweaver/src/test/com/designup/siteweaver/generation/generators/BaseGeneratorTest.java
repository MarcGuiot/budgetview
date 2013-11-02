package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;
import com.designup.siteweaver.model.Page;

public class BaseGeneratorTest extends GeneratorTestCase {

  protected Generator getGenerator() {
    return new BaseGenerator();
  }

  public void test() throws Exception {
    String result = generate(rootPage);
    assertTrue(result.startsWith("file://"));
    assertTrue(result.endsWith("/tmp/output/"));
  }
}
