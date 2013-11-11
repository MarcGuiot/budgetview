package com.designup.siteweaver.html;

import java.util.HashMap;
import java.util.Map;

public class HtmlTag {

  private String name;
  private Map<String, String> attrsTable = new HashMap<String, String>();

  public void setName(String tagName) {
    name = tagName.toLowerCase();
  }

  public String getName() {
    return name;
  }

  public void addAttribute(String attrName, String attrValue) {
    attrsTable.put(attrName.toLowerCase(), attrValue);
  }

  public boolean hasAttribute(String attrName) {
    return attrsTable.containsKey(attrName);
  }

  public String getAttributeValue(String attrName) {
    return attrsTable.get(attrName);
  }

  public boolean isTrue(String attrName, boolean defaultValue) {
    if (!attrsTable.containsKey(attrName)) {
      return defaultValue;
    }
    return attrsTable.get(attrName).equalsIgnoreCase("true");
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

  /**
   * Adds a representation of a tag's attribute to a given string. The
   * attribute is represented with an "name=value" format. Nothing is added
   * if the attribute is not present in the tag.
   */
  public void addFormattedAttribute(String attrName,
                                    String outputName,
                                    StringBuffer outputString) {
    String attrValue = getAttributeValue(attrName);
    if (attrValue != null) {
      outputString.append(" " + outputName + "=\"" + attrValue + "\"");
    }
  }

  public void addFormattedAttribute(String attrName,
                                    String outputName,
                                    String defaultValue,
                                    StringBuffer outputString) {
    String attrValue = getAttributeValue(attrName);
    String value = attrValue != null ? attrValue : defaultValue;
    outputString.append(" " + outputName + "=\"" + value + "\"");
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("<" + name);
    for (Map.Entry<String, String> entry : attrsTable.entrySet()) {
      builder.append(" "+ entry.getKey() + "=\"" + entry.getValue() + "\"");
    }
    builder.append(">");
    return builder.toString();
  }
}
