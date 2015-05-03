package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.TitleGenerator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupTitleFormatter implements TitleGenerator.Formatter {

  private final String prefix;
  private final String postfix;

  public DupTitleFormatter(HtmlTag tag) {
    prefix = tag.getAttributeValue("prefix", "");
    postfix = tag.getAttributeValue("postfix", "");
  }

  public void writeTitle(Page page, String title, HtmlWriter writer) {
    String adjustedTitle = title.replace(" ?", "&nbsp;?").replace(" :", "&nbsp;:");
    writer.write("<title>" + prefix + adjustedTitle + postfix + "</title>\n");
  }
}
