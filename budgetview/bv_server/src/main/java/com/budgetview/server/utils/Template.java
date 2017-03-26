package com.budgetview.server.utils;

public class Template {
  private String content;

  public static Template init(String content) {
    return new Template(content);
  }

  private Template(String content) {
    this.content = content;
  }

  public Template set(String key, String value) {
    content = content.replace("{{" + key + "}}", value != null ? value : "");
    return this;
  }

  public String get() {
    return content;
  }
}
