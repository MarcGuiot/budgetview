package org.designup.picsou.bank.connectors.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.connectors.webcomponents.WebBrowser;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlUnit {
  private static final Pattern DISPLAY_NONE = Pattern.compile(".*display:[ ]*none.*");

  private static final String SEPARATOR = "------------------------------------------------------------------------\n";

  public interface Filter {
    boolean matches(HtmlElement element);
  }

  public static <T extends HtmlElement> T getElementById(HtmlElement container, String id, Class<T> expectedClass) throws WebParsingError {
    T result = null;
    try {
      result = container.getElementById(id);
    }
    catch (com.gargoylesoftware.htmlunit.ElementNotFoundException e) {
      fail(container, "Cannot find element with id: " + id +
                      " - actual panel content:\n" + dump(container));
    }
    checkClass(container, expectedClass, result, "element '" + id + "'");
    return result;
  }

  public static <T extends HtmlElement> T getElementById(HtmlPage currentPage, String id, Class<T> expectedClass) throws WebParsingError {
    return getElementById(currentPage.getDocumentElement(), id, expectedClass);
  }

  public static HtmlElement getSingleElementByTag(HtmlElement container, String tagName, Class expectedClass) throws WebParsingError {
    List list = container.getHtmlElementsByTagName(tagName);
    if (list.isEmpty()) {
      fail(container, "No <" + tagName + "> found - actual content:\n" + dump(container));
    }
    if (list.size() > 1) {
      fail(container, "Too many <" + tagName + "> found - actual list= " + list +
                      "\nactual content:\n" + dump(container));
    }
    HtmlElement result = (HtmlElement)list.get(0);
    checkClass(container, expectedClass, result, "<" + tagName + ">");
    return result;
  }

  public static HtmlElement getElementWithAttribute(HtmlElement container,
                                                    String tagName,
                                                    String attributeName,
                                                    String attributeValue) throws WebParsingError {
    List list = getVisibleElementsWithAttribute(container, tagName, attributeName, attributeValue);
    if (list.isEmpty()) {
      fail(container, "No <" + tagName + "> found with " + attributeName + "=" + attributeValue);
    }
    if (list.size() > 1) {
      fail(container, "Too many <" + tagName + "> found with " + attributeName + "=" + attributeValue + " - actual = " + list);
    }
    return (HtmlElement)list.get(0);
  }

  public static HtmlElement getFirstElementWithAttribute(HtmlElement container,
                                                         String tagName,
                                                         String attributeName,
                                                         String attributeValue) throws WebParsingError {
    List list = getVisibleElementsWithAttribute(container, tagName, attributeName, attributeValue);
    if (list.isEmpty()) {
      fail(container, "No <" + tagName + "> found with " + attributeName + "=" + attributeValue);
    }
    return (HtmlElement)list.get(0);
  }

  public static HtmlElement getElementWithAttribute(HtmlElement container,
                                                    Class expectedClass,
                                                    String tagName,
                                                    String attributeName,
                                                    String attributeValue) throws WebParsingError {
    HtmlElement result = getElementWithAttribute(container, tagName, attributeName, attributeValue);
    checkClass(container, expectedClass, result, "<" + tagName + "> with " + attributeName + "=" + attributeValue);
    return result;
  }

  public static <T extends HtmlElement> T getFirstElementWithAttribute(HtmlElement container, Class<T> expectedClass, String tagName, String attributeName, String attributeValue) throws WebParsingError {
    HtmlElement result = getFirstElementWithAttribute(container, tagName, attributeName, attributeValue);
    checkClass(container, expectedClass, result, "<" + tagName + "> with " + attributeName + "=" + attributeValue);
    return (T)result;
  }

  public static HtmlElement getFirstParent(HtmlElement element, String tag) throws WebParsingError {
    for (DomNode parent = element.getParentNode(); parent != null; parent = parent.getParentNode()) {
      String tagName = parent.getNodeName();
      if (tag.equalsIgnoreCase(tagName)) {
        return (HtmlElement)parent;
      }
    }
    throw new WebParsingError(element, "No parent found with tag <" + tag + ">");
  }

  private static void checkClass(HtmlElement container,
                                 Class expectedClass,
                                 HtmlElement result,
                                 String elementName) throws WebParsingError {
    if (!expectedClass.isAssignableFrom(result.getClass())) {
      fail(container, "Actual HtmlElement for " + elementName + " is " +
                      result.getClass().getName() + " instead of " + expectedClass.getName() +
                      "- actual content:\n" + dump(container));
    }
  }

  public static List<HtmlElement> getVisibleElementsWithAttribute(HtmlElement container, String tagName, String attributeName, String attributeValue) {
    List<HtmlElement> list = container.getHtmlElementsByTagName(tagName);
    for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
      HtmlElement element = (HtmlElement)iterator.next();
      if ((attributeName != null) &&
          (attributeValue != null) &&
          !attributeValue.equals(element.getAttribute(attributeName))) {
        iterator.remove();
      }
      else if (!isVisible(element)) {
        iterator.remove();
      }
    }
    return list;
  }

  public static <T extends HtmlElement> T getSingleElement(HtmlElement container, String tagName, Filter filter) throws WebParsingError {
    List<HtmlElement> result = getElements(container, tagName, filter);
    if (result.isEmpty()) {
      fail(container, "No <" + tagName + "> found matching filter");
    }
    if (result.size() > 1) {
      fail(container, "Too many <" + tagName + "> found matching filter");
    }
    return (T)result.get(0);

  }

  public static List<HtmlElement> getElements(HtmlElement container, String tagName, Filter filter) {
    List<HtmlElement> result = container.getHtmlElementsByTagName(tagName);
    for (Iterator iterator = result.iterator(); iterator.hasNext(); ) {
      HtmlElement element = (HtmlElement)iterator.next();
      if (!filter.matches(element)) {
        iterator.remove();
      }
    }
    return result;
  }

  public static boolean isVisible(HtmlElement element) {
    return !DISPLAY_NONE.matcher(element.getAttribute("style")).matches();
  }

  public static HtmlElement getSingleElementWithText(HtmlElement container, String tagName, String text) throws WebParsingError {
    List result = getElementsWithText(container, tagName, text);
    if (result.isEmpty()) {
      fail(container, "No <" + tagName + "> found with text: " + text);
    }
    if (result.size() > 1) {
      fail(container, "Too many <" + tagName + "> found with text: " + text + " - actual = " + result);
    }
    return (HtmlElement)result.get(0);
  }

  public static List getElementsWithText(HtmlElement container, String tagName, String text) throws WebParsingError {
    return container.getByXPath("//" + tagName + "//[.=" + generateConcatForXPath(text) + "]");
  }

  private static String generateConcatForXPath(String searchString) {
    String result = "\'" + searchString + "\'";
    if (searchString.indexOf("\'") != -1) {
      StringBuffer buffer = new StringBuffer("concat(\'");
      buffer.append(searchString.replaceAll("[\']", "\',\"\'\",\'"));
      buffer.append("\')");
      result = buffer.toString();
    }
    else {
      return result;
    }
    return result;
  }

  public static String dump(DomNode element) {
    HtmlPage page = (HtmlPage)element.getPage();
    StringBuffer buffer = new StringBuffer();
    buffer.append(SEPARATOR);
    buffer.append(page.getUrl());
    buffer.append("\n");
    buffer.append(page.getTitleText());
    buffer.append("\n");
    buffer.append(SEPARATOR);
    buffer.append(element.asXml().replaceAll("\n[\\s]*\n", "\n"));
    buffer.append(SEPARATOR);
    return buffer.toString();
  }

  public static HtmlElement getElementByLabel(String text, HtmlElement container) throws WebParsingError {
    return getElementByLabel(text, container, HtmlElement.class);
  }

  public static <T extends HtmlElement> T getElementByLabel(String text, HtmlElement container, Class<T> expectedClass) throws WebParsingError {
    String targetElementId = getTargetIdFromLabel(container, text);
    T element = container.getElementById(targetElementId);
    if (!expectedClass.isAssignableFrom(element.getClass())) {
      fail(container, "Unexpected class '" + element.getClass().getName() + "' for component with label: " + text +
                      " - actual content: " + dump(container));
    }
    return element;
  }

  public static String getTargetIdFromLabel(HtmlElement container, String text) throws WebParsingError {
    HtmlLabel label = (HtmlLabel)getSingleElementWithText(container, "label", text);
    return label.getAttribute("for");
  }

  private static void fail(HtmlElement container, String message) throws WebParsingError {
    throw new WebParsingError(container, message);
  }

  public static void click(WebBrowser browser, HtmlElement element) throws WebParsingError {
    try {
      browser.setCurrentPage(element.click());
    }
    catch (IOException e) {
      fail(element, "while clicking");
    }
  }
}
