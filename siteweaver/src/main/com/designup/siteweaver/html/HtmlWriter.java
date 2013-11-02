package com.designup.siteweaver.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class HtmlWriter extends PrintWriter {

  public HtmlWriter(Writer writer) {
    super(writer);
  }

  public HtmlWriterTagBuilder startTag(String name) {
    return new HtmlWriterTagBuilder(name);
  }

  public void closeTag(String name) {
    write("</");
    write(name);
    write('>');
  }

  public void writeTag(String tagName) {
    write('<');
    write(tagName);
    write('>');
  }

  public class HtmlWriterTagBuilder {

    public HtmlWriterTagBuilder(String name) {
      write('<');
      write(name);
    }

    public HtmlWriterTagBuilder add(String attrName, String value) {
      if (value == null) {
        return this;
      }
      write(' ');
      write(attrName);
      write("=\"");
      write(value);
      write('"');
      return this;
    }

    public void end() {
      write('>');
    }
  }

  public HtmlWriter writeLink(String text, String url) throws IOException {
    if ((url == null) && (text != null)) {
      return this;
    }
    write("<a href=\"");
    write(url);
    write("\">");
    write(text);
    write("</a>");
    return this;
  }
}
