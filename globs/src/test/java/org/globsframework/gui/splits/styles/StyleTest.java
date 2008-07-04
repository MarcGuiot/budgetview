package org.globsframework.gui.splits.styles;

import junit.framework.TestCase;

public class StyleTest extends TestCase {
  public void testOneLevel() throws Exception {
    assertMatches("type", "type");
    assertMatches(".class", "type.class");
    assertMatches("#name", "type#name");
  }

  public void testOneLevelMismatches() throws Exception {
    assertNoMatch("type", "anotherType");
    assertNoMatch("type.class", "type.anotherClass");
    assertNoMatch("type#name", "type#anotherName");
  }

  public void testContainmentMatches() throws Exception {
    assertMatches("parent type", "parent.class type.class");
    assertMatches("ancestor type", "ancestor parent type");
    assertMatches(".class type", "ancestor.class parent.anotherClas anotherParent#name type");
    assertMatches("#name type", "ancestor#name parent.anotherClas anotherParent#name type");
  }

  public void testContainmentMismatches() throws Exception {
    assertNoMatch("parent type", "anotherParent type");
    assertNoMatch(".class", "parent.class type.anotherClass");
  }

  private void assertMatches(String styleSelectors, String pathSelectors) {
    checkMatches(true, styleSelectors, pathSelectors);
  }

  private void assertNoMatch(String styleSelectors, String pathSelectors) {
    checkMatches(false, styleSelectors, pathSelectors);
  }

  private void checkMatches(boolean matches, String styleSelectors, String pathSelectors) {
    Style style = new Style(Selector.parseSequence(styleSelectors), null);
    SplitsPath path = new SplitsPath(Selector.parseSequence(pathSelectors));
    assertEquals(matches, style.matches(path));
  }
}
