package org.designup.picsou.bank.connectors.webcomponents.filters;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class WebFilters {

  public static WebFilter and(final WebFilter... filters) {
    return new WebFilter() {
      public boolean matches(HtmlElement element) {
        for (WebFilter filter : filters) {
          if (filter != null && !filter.matches(element)) {
            return false;
          }
        }
        return true;
      }

      public String toString() {
        StringBuilder builder = new StringBuilder();
        for (WebFilter filter : filters) {
          if (filter != null) {
            builder.append(filter.toString()).append(" and ");
          }
        }
        String s = builder.toString();
        return s.length() == 0 ? s : s.substring(0, s.length() - 5);
      }
    };
  }

  public static WebFilter typeEquals(final String type) {
    return new WebFilter() {
      public boolean matches(HtmlElement element) {
        return element.getAttribute("type").equalsIgnoreCase(type);
      }

      public String toString() {
        return "type = " + type;
      }
    };
  }

  public static WebFilter attributeEquals(final String attrName, final String value) {
    return new WebFilter() {
      public boolean matches(HtmlElement element) {
        return element.getAttribute(attrName).equalsIgnoreCase(value);
      }

      public String toString() {
        return attrName + " == " + value;
      }
    };
  }

  public static WebFilter refStartsWith(final String text) {
    final String s = text.toLowerCase();
    return new WebFilter() {
      public boolean matches(HtmlElement element) {
        return element.getAttribute("href").toLowerCase().startsWith(s);
      }

      public String toString() {
        return "ref starts with " + text;
      }
    };
  }

  public static WebFilter textContentContains(final String value) {
    return new WebFilter() {
      public boolean matches(HtmlElement element) {
        return element.getTextContent().contains(value);
      }

      public String toString() {
        return "text content contains " + value;
      }
    };
  }

  public static WebFilter tagEquals(final String tagName) {
    return new WebFilter() {
      public boolean matches(HtmlElement element) {
        return element.getTagName().equalsIgnoreCase(tagName);
      }

      public String toString() {
        return "tagName == " + tagName;
      }
    };
  }
}
