package org.crossbowlabs.globs.sqlstreams.utils;

public interface PrettyWriter {
  StringPrettyWriter append(String s);

  void appendIf(String s, boolean shouldAppend);

  PrettyWriter newLine();
}
