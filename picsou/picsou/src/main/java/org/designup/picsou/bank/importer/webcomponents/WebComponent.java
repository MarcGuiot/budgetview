package org.designup.picsou.bank.importer.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.designup.picsou.bank.importer.webcomponents.utils.HtmlUnit;

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
}
