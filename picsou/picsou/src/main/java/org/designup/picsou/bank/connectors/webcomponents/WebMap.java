package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlMap;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

public class WebMap extends WebComponent<HtmlMap>{
  protected WebMap(WebBrowser browser, HtmlMap node) {
    super(browser, node);
  }

  public WebMapArea getAreaById(String id) throws WebParsingError {
    return new WebMapArea(browser, HtmlUnit.getElementById(node, id, HtmlArea.class));
  }

  public class WebMapArea extends WebComponent<HtmlArea> {
    public WebMapArea(WebBrowser browser, HtmlArea id) {
      super(browser, id);
    }
  }
}
