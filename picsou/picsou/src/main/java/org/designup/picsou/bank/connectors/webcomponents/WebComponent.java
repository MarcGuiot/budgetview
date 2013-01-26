package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import sun.text.normalizer.TrieIterator;

import java.io.IOException;

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

  public String asXml() {
    return node.asXml();
  }

  public String toString() {
    return HtmlUnit.dump(node);
  }

  public void removeAttribute(String attributeName) {
    node.removeAttribute(attributeName);
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

  public String getOnclick() {
    return node.getOnClickAttribute();
  }

  public T getNode() {
    return node;
  }

  static public class HtmlNavigate {
    private WebBrowser browser;
    private HtmlElement node;
    private final boolean optional;

    public HtmlNavigate(WebBrowser browser, HtmlElement node, boolean optional) {
      this.browser = browser;
      this.node = node;
      this.optional = optional;
    }

    public HtmlNavigate next() throws WebParsingError {
      if (optional && node == null){
        return this;
      }
      node = (HtmlElement)findFirstHtmlElement(node.getNextSibling());
      return this;
    }

    private DomNode findFirstHtmlElement(DomNode sibling) throws WebParsingError {
      while (!(sibling instanceof HtmlElement) && sibling != null){
        sibling = sibling.getNextSibling();
      }
      if (sibling == null){
        throw new WebParsingError(node, "Can not find next");
      }
      return sibling;
    }

    public HtmlNavigate in() throws WebParsingError {
      if (optional && node == null){
        return this;
      }
      node = (HtmlElement)findFirstHtmlElement(node.getFirstChild());
      return this;
    }

    public WebAnchor asAnchor() throws WebParsingError {
      if (optional && node == null){
        return null;
      }
      if (node instanceof HtmlAnchor){
        return new WebAnchor(browser, ((HtmlAnchor)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public WebCheckBox asCheckBox() throws WebParsingError {
      if (optional && node == null){
        return null;
      }
      if (node instanceof HtmlInput){
        return new WebCheckBox(browser, ((HtmlInput)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }
  }

  public HtmlNavigate navigate() {
    return new HtmlNavigate(browser, node, false);
  }
}
