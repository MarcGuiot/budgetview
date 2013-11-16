package com.designup.siteweaver.html;

import java.util.HashMap;
import java.util.Map;

public class HtmlTag {

  private String name;
  private Map<String, String> attributes = new HashMap<String, String>();

  void setName(String tagName) {
    name = tagName.toLowerCase();
  }

  public String getTagName() {
    return name;
  }

  public void addAttribute(String attrName, String attrValue) {
    attributes.put(attrName.toLowerCase(), attrValue);
  }

  public boolean containsAttribute(String attrName) {
    return attributes.containsKey(attrName);
  }

  public String getAttributeValue(String attrName) {
    return attributes.get(attrName);
  }

  public int getIntValue(String attrName) throws IllegalArgumentException {
    String result = attributes.get(attrName);
    try {
      return Integer.parseInt(result);
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Attribute '" + attrName + "' must be an integer", e);
    }
  }

  public boolean isTrue(String attrName, boolean defaultValue) {
    if (!attributes.containsKey(attrName)) {
      return defaultValue;
    }
    return attributes.get(attrName).equalsIgnoreCase("true");
  }

  public boolean isBooleanAttributeSet(String attrName) {
    String val = getAttributeValue(attrName, "");
    return (val.toLowerCase().equals("yes") || val.toLowerCase().equals("true"));
  }

  public String getAttributeValue(String attrName, String defaultValue) {
    String attributeValue = getAttributeValue(attrName);
    if (attributeValue == null) {
      return defaultValue;
    }
    return attributeValue;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("<" + name);
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      builder.append(" "+ entry.getKey() + "=\"" + entry.getValue() + "\"");
    }
    builder.append(">");
    return builder.toString();
  }
}
