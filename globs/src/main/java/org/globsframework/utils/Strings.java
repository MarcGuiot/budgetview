package org.globsframework.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Strings {
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private Strings() {
  }

  public static String toString(Object value) {
    return value != null ? value.toString() : "";
  }

  public static boolean isNotEmpty(String text) {
    return (text != null) && (text.length() != 0);
  }

  public static boolean isNullOrEmpty(String text) {
    return (text == null) || (text.length() == 0);
  }

  public static String capitalize(String value) {
    if ((value == null) || "".equals(value)) {
      return value;
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1, value.length());
  }

  public static String uncapitalize(String value) {
    if ((value == null) || "".equals(value)) {
      return value;
    }
    return value.substring(0, 1).toLowerCase() + value.substring(1, value.length());
  }

  public static String toNiceLowerCase(String value) {
    boolean upperCaseNext = false;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '_') {
        upperCaseNext = true;
      }
      else {
        builder.append(upperCaseNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
        upperCaseNext = false;
      }
    }
    return builder.toString();
  }

  public static String toNiceUpperCase(String value) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if ((i > 0) && Character.isUpperCase(c) && Character.isLowerCase(value.charAt(i - 1))) {
        builder.append("_");
      }
      builder.append(Character.toUpperCase(c));
    }
    return builder.toString();
  }

  public static String toString(Map map) {
    List<String> lines = new ArrayList<String>();
    for (Object o : map.entrySet()) {
      Map.Entry entry = (Map.Entry)o;
      lines.add("'" + toString(entry.getKey()) + "' = '" + toString(entry.getValue()) + "'");
    }
    Collections.sort(lines);
    StringBuilder builder = new StringBuilder();
    for (String line : lines) {
      builder.append(line).append(LINE_SEPARATOR);
    }
    return builder.toString();
  }

  public static String join(String... items) {
    StringBuilder buf = new StringBuilder();
    for (String item : items) {
      if (isNullOrEmpty(item)) {
        continue;
      }
      if (buf.length() != 0) {
        buf.append(" ");
      }
      buf.append(item);
    }
    return buf.toString();
  }

  public static String nullIfEmpty(String text) {
    if (isNullOrEmpty(text)) {
      return null;
    }
    return text;
  }
}
