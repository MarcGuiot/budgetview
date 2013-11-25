package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

public class WebListItem extends WebComponent<HtmlListItem> {

  protected WebListItem(WebBrowser browser, HtmlListItem input) {
    super(browser, input);
  }
}
