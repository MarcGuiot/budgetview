package org.designup.picsou.bank.connectors.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.connectors.webcomponents.WebBrowser;
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlUnit {
  private static final Pattern DISPLAY_NONE = Pattern.compile(".*display:[ ]*none.*");

  private static final String SEPARATOR = "------------------------------------------------------------------------\n";

  public static <T extends HtmlElement> T getElementById(HtmlElement container, String id, Class<T> expectedClass) throws WebParsingError {
    T result = null;
    try {
      result = container.<T>getElementById(id);
    }
    catch (com.gargoylesoftware.htmlunit.ElementNotFoundException e) {
      fail(container, "Cannot find element with id: " + id);
    }
    checkClass(container, result, "element '" + id + "'", expectedClass);
    return result;
  }

  public static <T extends HtmlElement> T getElementById(HtmlPage currentPage, String id, Class<T> expectedClass) throws WebParsingError {
    return getElementById(currentPage.getDocumentElement(), id, expectedClass);
  }

  public static <T extends HtmlElement> T getSingleElementByTag(HtmlElement container, String tagName, Class<T> expectedClass) throws WebParsingError {
    List list = container.getHtmlElementsByTagName(tagName);
    if (list.isEmpty()) {
      fail(container, "No <" + tagName + "> found");
    }
    if (list.size() > 1) {
      fail(container, "Too many <" + tagName + "> found - actual list= " + list);
    }
    HtmlElement result = (HtmlElement)list.get(0);
    checkClass(container, result, "<" + tagName + ">", expectedClass);
    return (T)result;
  }

  public static <T extends HtmlElement> T getElementWithAttribute(HtmlElement container,
                                                                  String tagName,
                                                                  String attributeName,
                                                                  String attributeValue,
                                                                  Class<T> expectedClass) throws WebParsingError {
    List list = getVisibleElementsWithAttribute(container, tagName, attributeName, attributeValue);
    if (list.isEmpty()) {
      fail(container, "No <" + tagName + "> found with " + attributeName + "=" + attributeValue);
    }
    if (list.size() > 1) {
      fail(container, "Too many <" + tagName + "> found with " + attributeName + "=" + attributeValue + " - actual = " + list);
    }
    T result = (T)list.get(0);
    checkClass(container, result, tagName, expectedClass);
    return result;
  }

  public static <T extends HtmlElement> T getFirstElementWithAttribute(HtmlElement container,
                                                                       Class<T> expectedClass,
                                                                       String tagName,
                                                                       String attributeName,
                                                                       String attributeValue) throws WebParsingError {
    List list = getVisibleElementsWithAttribute(container, tagName, attributeName, attributeValue);
    if (list.isEmpty()) {
      fail(container, "No <" + tagName + "> found with " + attributeName + "=" + attributeValue);
    }
    HtmlElement result = (HtmlElement)list.get(0);
    checkClass(container, result, "<" + tagName + "> with " + attributeName + "=" + attributeValue, expectedClass);
    return (T)result;
  }

  public static <T extends HtmlElement> T getFirstParent(HtmlElement element, String tagName, Class<T> expectedClass) throws WebParsingError {
    for (DomNode parent = element.getParentNode(); parent != null; parent = parent.getParentNode()) {
      HtmlElement parentElement = (HtmlElement)parent;
      String parentTagName = parent.getNodeName();
      if (tagName.equalsIgnoreCase(parentTagName)) {
        checkClass(parentElement, parentElement, tagName, expectedClass);
        return (T)parent;
      }
    }
    throw new WebParsingError(element, "No parent found with tag <" + tagName + ">");
  }

  private static void checkClass(HtmlElement container, HtmlElement element, String tagName, Class expectedClass) throws WebParsingError {
    if (!expectedClass.isAssignableFrom(element.getClass())) {
      fail(container, "Actual HtmlElement for " + tagName + " is " +
                      element.getClass().getName() + " instead of " + expectedClass.getName());
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

  public static <T extends HtmlElement> T getSingleElement(HtmlElement container, String tagName, WebFilter filter) throws WebParsingError {
    List<HtmlElement> result = getElements(container, tagName, filter);
    if (result.isEmpty()) {
      fail(container, "No <" + tagName + "> found matching filter '" + filter + "'");
    }
    if (result.size() > 1) {
      fail(container, "Too many <" + tagName + "> found matching filter '" + filter + "'");
    }
    return (T)result.get(0);
  }

  public static HtmlElement findHtmlElement(HtmlElement container, WebFilter filter) throws WebParsingError {
    return getHtmlElement(container, filter, true);
  }

  public static HtmlElement getHtmlElement(HtmlElement container, WebFilter filter) throws WebParsingError {
    return getHtmlElement(container, filter, false);
  }

  public static HtmlElement findFirstHtmlElement(HtmlElement container, WebFilter filter) throws WebParsingError {
    Iterable<HtmlElement> elementDescendants = container.getHtmlElementDescendants();
    for (HtmlElement element : elementDescendants) {
      if (filter.matches(element)) {
        return element;
      }
    }
    return null;
  }

  public static List<HtmlElement> findAllHtmlElement(HtmlElement container, WebFilter filter) throws WebParsingError {
    Iterable<HtmlElement> elementDescendants = container.getHtmlElementDescendants();
    ArrayList<HtmlElement> elements = new ArrayList<HtmlElement>();
    for (HtmlElement element : elementDescendants) {
      if (filter.matches(element)) {
        elements.add(element);
      }
    }
    return elements;
  }

  public static HtmlElement getHtmlElement(HtmlElement container, WebFilter filter, boolean nullIfNotFound) throws WebParsingError {
    Iterable<HtmlElement> elementDescendants = container.getHtmlElementDescendants();
    List<HtmlElement> result = new ArrayList<HtmlElement>();
    for (HtmlElement element : elementDescendants) {
      if (filter.matches(element)) {
        result.add(element);
      }
    }
    if (result.isEmpty()) {
      if (nullIfNotFound) {
        return null;
      }
      fail(container, "No <" + filter + "> found matching filter '" + filter + "'");
    }
    if (result.size() > 1) {
      fail(container, "Too many <" + filter + "> found matching filter '" + filter + "'");
    }
    return result.get(0);
  }

  public static List<HtmlElement> getElements(HtmlElement container, String tagName, WebFilter filter) {
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
    buffer.append("\n")
      .append(SEPARATOR)
      .append(page.getUrl())
      .append("\n")
      .append(page.getTitleText())
      .append("\n")
      .append(SEPARATOR)
      .append(element.asText().replaceAll("\n[\\s]*\n", "\n").replaceAll("[0-9]", "9"))
      .append(SEPARATOR)
      .append(element.asXml().replaceAll("\n[\\s]*\n", "\n").replaceAll("[0-9]", "9"))
      .append(SEPARATOR);
    return buffer.toString();
  }

  public static HtmlElement getElementByLabel(String text, HtmlElement container) throws WebParsingError {
    return getElementByLabel(text, container, HtmlElement.class);
  }

  public static <T extends HtmlElement> T getElementByLabel(String text, HtmlElement container, Class<T> expectedClass) throws WebParsingError {
    String targetElementId = getTargetIdFromLabel(container, text);
    T element = container.<T>getElementById(targetElementId);
    if (!expectedClass.isAssignableFrom(element.getClass())) {
      fail(container,
           "Unexpected class '" + element.getClass().getName() + "' for component with label: " + text);
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
