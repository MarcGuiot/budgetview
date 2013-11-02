package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

/**
 * Writes the value associated to a given key for the current page.
 */
public class ValueGenerator implements Generator {

  private String key;
  private boolean inherited;

  public ValueGenerator(HtmlTag tag) {
    if (tag.hasAttribute("key")) {
      key = tag.getAttributeValue("key");
    }

    if (tag.hasAttribute("inherited")) {
      inherited = tag.isBooleanAttributeSet("inherited");
    }
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    if (key == null) {
      return;
    }
    String value = page.getValueForKey(key, inherited);
    if (value != null) {
      writer.write(value);
    }
  }
}
