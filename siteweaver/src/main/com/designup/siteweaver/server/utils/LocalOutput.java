package com.designup.siteweaver.server.utils;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LocalOutput implements HtmlOutput {

  private PrintWriter writer;

  public LocalOutput(PrintWriter writer) {
    this.writer = writer;
  }

  public HtmlWriter createWriter(Page page) throws IOException {
    return new HtmlWriter(writer);
  }

  public String getBaseUrl(Site site) {
    return "http://localhost:8080";
  }

  public void copyFile(File inputFile, String filePath) throws IOException {
    throw new RuntimeException("unexpected call");
  }
}
