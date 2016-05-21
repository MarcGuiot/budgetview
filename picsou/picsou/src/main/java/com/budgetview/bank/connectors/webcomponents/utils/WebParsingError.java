package com.budgetview.bank.connectors.webcomponents.utils;

import com.budgetview.bank.connectors.webcomponents.WebComponent;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

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
