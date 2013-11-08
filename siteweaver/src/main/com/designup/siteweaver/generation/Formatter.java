package com.designup.siteweaver.generation;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public interface Formatter {

  public void writeStart(HtmlWriter writer) throws IOException;

  public void writeElement(Page page, Page target, HtmlWriter writer) throws IOException;

  public void writeSeparator(HtmlWriter writer) throws IOException;

  public void writeEnd(HtmlWriter writer) throws IOException;
}
