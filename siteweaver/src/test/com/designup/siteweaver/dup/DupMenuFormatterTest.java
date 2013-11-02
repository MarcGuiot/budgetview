package com.designup.siteweaver.dup;

import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import junit.framework.TestCase;

import java.io.StringWriter;

public class DupMenuFormatterTest extends TestCase {
  public void test() throws Exception {
    HtmlTag tag = new HtmlTag();
    tag.addAttribute("face", "myface");
    tag.addAttribute("imgsrc", "image_source");
    DupMenuFormatter formatter = new DupMenuFormatter();
    StringWriter writer = new StringWriter();
    HtmlWriter htmlWriter = new HtmlWriter(writer);
    formatter.writeStart(htmlWriter);
  }
}
