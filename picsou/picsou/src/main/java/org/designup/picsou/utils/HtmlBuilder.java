package org.designup.picsou.utils;

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
