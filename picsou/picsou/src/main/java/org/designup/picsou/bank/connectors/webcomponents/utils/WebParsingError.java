package org.designup.picsou.bank.connectors.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.designup.picsou.bank.connectors.webcomponents.WebComponent;

public class WebParsingError extends Exception {

  public WebParsingError(WebComponent component, String message) {
    super(message + HtmlUnit.dump(component.getNode()));
  }

  public WebParsingError(WebComponent component, Throwable e) {
    super(e.getMessage() + HtmlUnit.dump(component.getNode()), e);
  }

  public WebParsingError(HtmlElement container, String message) {
    super(message + HtmlUnit.dump(container));
  }
}
