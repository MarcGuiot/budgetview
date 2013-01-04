package org.designup.picsou.bank.connectors.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.designup.picsou.bank.connectors.webcomponents.WebComponent;
import org.globsframework.utils.exceptions.GlobsException;

import java.net.MalformedURLException;

public class WebParsingError extends Exception {
  public final String url;

  public WebParsingError(WebComponent component, String message) {
    super(message);
    url = component.getUrl();
  }

  public WebParsingError(HtmlElement container, String message) {
    super(message);
    url = container.getPage().getUrl().toString();
  }

  public WebParsingError(String url, String message) {
    super(message);
    this.url = url;
  }

  public WebParsingError(String url, Exception e) {
    super(e);
    this.url = url;
  }
}
