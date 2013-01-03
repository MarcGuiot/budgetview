package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;

public abstract class WebComponent<T extends HtmlElement> {
  protected final WebBrowser browser;
  protected final T node;

  protected WebComponent(WebBrowser browser, T node) {
    this.browser = browser;
    this.node = node;
  }

  public String dump(HtmlElement element) {
    return element.toString();
  }

  public String getUrl() {
    return browser.getUrl();
  }

  public String asText() {
    return node.asText();
  }

  public String toString() {
    return HtmlUnit.dump(node);
  }

  public void fireEvent(String event) {
    node.fireEvent(event);
  }

  public String getClassName(){
    return node.getAttribute("className");
  }

  public void mouseDown() {
    browser.setCurrentPage(node.mouseDown());
  }

  public void mouseUp() {
    browser.setCurrentPage(node.mouseUp());
  }

  public void mouseOut() {
    browser.setCurrentPage(node.mouseOut());
  }

  public void mouseOver() {
    browser.setCurrentPage(node.mouseOver());
  }

  public T getNode() {
    return node;
  }
}
