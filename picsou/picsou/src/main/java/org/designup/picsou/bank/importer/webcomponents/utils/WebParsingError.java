package org.designup.picsou.bank.importer.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.designup.picsou.bank.importer.webcomponents.WebComponent;
import org.globsframework.utils.exceptions.GlobsException;

public class WebParsingError extends GlobsException {
  public final String url;

  public WebParsingError(WebComponent component, String message) {
    super(message);
    url = component.getUrl();
  }

  public WebParsingError(HtmlElement container, String message) {
    super(message);
    url = container.getPage().getUrl().toString();
  }
}
