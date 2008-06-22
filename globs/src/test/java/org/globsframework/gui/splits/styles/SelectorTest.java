package org.globsframework.gui.splits.styles;

import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidFormat;

public class SelectorTest extends TestCase {
  public void test() throws Exception {
    TestUtils.assertEquals(
      new Selector[]{
        new Selector("type", null, null),
        new Selector("type", "comp", null),
        new Selector("type", null, "class"),
        new Selector(null, "comp", null),
        new Selector(null, null, "class"),
      },
      Selector.parseSequence(" type  type#comp   type.class  #comp .class "));
  }

  public void testInvalidFormats() throws Exception {
    checkInvalidFormats("type.",
                        "type#",
                        "type.#toto",
                        ".",
                        "#");
  }

  public void testMatching() throws Exception {
    Selector sample = new Selector("type", "name", "class");
    assertMatches(true, "type", sample);
    assertMatches(true, ".class", sample);
    assertMatches(true, "#name", sample);
    assertMatches(true, "type.class", sample);
    assertMatches(true, "type#name", sample);

    assertMatches(false, "anotherType", sample);
    assertMatches(false, ".anotherClass", sample);
    assertMatches(false, "#anotherName", sample);
    assertMatches(false, "type.anotherClass", sample);
    assertMatches(false, "type#anotherName", sample);
    assertMatches(false, "anotherType.class", sample);
    assertMatches(false, "anotherType#name", sample);
  }

  private void assertMatches(boolean matches, String filter, Selector selector) {
    assertEquals(matches, Selector.parseSingle(filter).matches(selector));
  }

  private void checkInvalidFormats(String... samples) {
    for (String sample : samples) {
      try {
        Selector.parseSequence(sample);
        fail("Error not detected for:" + sample);
      }
      catch (InvalidFormat e) {
        // OK
      }
    }
  }
}
