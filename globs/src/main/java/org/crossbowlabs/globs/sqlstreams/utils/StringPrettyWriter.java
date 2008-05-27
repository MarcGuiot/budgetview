package org.crossbowlabs.globs.sqlstreams.utils;

public class StringPrettyWriter implements PrettyWriter {
  private StringBuilder builder = new StringBuilder();

  public StringPrettyWriter append(String s) {
    builder.append(s);
    return this;
  }

  public void appendIf(String s, boolean shouldAppend) {
    if (shouldAppend) {
      append(s);
    }
  }

  public String toString() {
    return builder.toString();
  }

  public PrettyWriter newLine() {
    builder.append("\n");
    return this;
  }
}
