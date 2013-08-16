package org.globsframework.gui.splits.utils;

public class HtmlUtils {
  public static String cleanup(String html) {
    return html
      .replaceAll("<br[ /]*>", " ")
      .replaceAll("<[^<>]+>", "")
      .replaceAll("&nbsp;", " ")
      .replaceAll("\n", " ")
      .replaceAll("\t", " ")
      .replaceAll("[ ]+", " ")
      .replace("", "")
      .replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&quot;", "\"")
      .replace("&apos;", "'")
      .trim();
  }
}
