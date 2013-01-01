package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.DomNodeFilter;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.util.List;

public class WebPanel extends WebContainer<HtmlElement> {

  public WebPanel(WebBrowser browser, HtmlElement element) {
    super(browser, element);
  }
}
