package com.designup.siteweaver.generation;

import com.designup.siteweaver.html.HtmlWriter;

import java.io.IOException;

public abstract class AbstractFormatter implements Formatter {
  public void writeStart(HtmlWriter output) throws IOException {
  }

  public void writeSeparator(HtmlWriter output) throws IOException {
  }

  public void writeEnd(HtmlWriter output) throws IOException {
  }
}
