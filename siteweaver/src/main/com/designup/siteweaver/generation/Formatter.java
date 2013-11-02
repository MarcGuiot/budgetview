package com.designup.siteweaver.generation;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public interface Formatter {

  public void writeStart(HtmlWriter output) throws IOException;

  public void writeElement(Page page, Page target, HtmlWriter output)
    throws IOException;

  public void writeSeparator(HtmlWriter output) throws IOException;

  public void writeEnd(HtmlWriter output) throws IOException;
}
