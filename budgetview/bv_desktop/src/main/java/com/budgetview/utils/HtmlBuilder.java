package com.budgetview.utils;

import org.globsframework.utils.exceptions.InvalidState;

public class HtmlBuilder {
  private StringBuilder builder = new StringBuilder();
  private boolean closed;

  public HtmlBuilder() {
    builder.append("<html>\n");
  }

  public HtmlBuilder appendParagraph(String text) {
    checkNotClosed();
    builder.append("<p>").append(text).append("</p>\n");
    return this;
  }
  
  public HtmlBuilder appendField(String field, String value) {
    checkNotClosed();
    builder.append("<p>").append(field).append(": ").append(value).append("</p>\n");
    return this;
  }

  public HtmlBuilder appendLine() {
    checkNotClosed();
    builder.append("<hr/>\n");
    return this;
  }

  public void close() {
    checkNotClosed();
    builder.append("</html>");
    closed = true;
  }

  private void checkNotClosed() {
    if (closed) {
      throw new InvalidState("Html stream is closed");
    }
  }

  public String toString() {
    if (!closed) {
      close();
    }
    return builder.toString();
  }
}
