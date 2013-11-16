package com.designup.siteweaver.html;

import junit.framework.TestCase;

public class HtmlTagTest extends TestCase {

  public void test() throws Exception {
    HtmlTag tag = new HtmlTag();
    tag.setName("toto");
    tag.addAttribute("attr1", "value1");
    tag.addAttribute("attr2", "value2");
    assertTrue(!tag.containsAttribute("titi"));
    assertTrue(tag.containsAttribute("attr1"));
    assertTrue(tag.getAttributeValue("attr1").equals("value1"));
    assertTrue(tag.getAttributeValue("titi") == null);
  }
}
