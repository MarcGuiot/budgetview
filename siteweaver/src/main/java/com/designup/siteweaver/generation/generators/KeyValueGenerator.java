package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class KeyValueGenerator implements Generator {

  public interface Formatter {

  }

  private String key;
  private boolean inherited;

  public KeyValueGenerator(HtmlTag tag) {
    if (tag.containsAttribute("key")) {
      key = tag.getAttributeValue("key");
    }

    if (tag.containsAttribute("inherited")) {
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
