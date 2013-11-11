package com.designup.siteweaver.server.utils;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class StringOutput implements HtmlOutput {

  private StringWriter writer;

  public HtmlWriter createWriter(Page page) throws IOException {
    writer = new StringWriter();
    return new HtmlWriter(writer);
  }

  public String getText() {
    return writer.toString();
  }

  public String getBaseUrl(Site site) {
    return site.getUrl();
  }

  public void copyFile(File inputFile, String filePath) throws IOException {
    throw new RuntimeException("unexpected call");
  }
}
