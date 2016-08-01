package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;

import java.net.MalformedURLException;

public class WebAnchor extends WebComponent<HtmlAnchor> {

  public WebAnchor(WebBrowser browser, HtmlAnchor anchor) {
    super(browser, anchor);
  }

  public String getTargetUrl() throws WebParsingError {
    try {
      return ((HtmlPage)node.getPage()).getFullyQualifiedUrl(node.getHrefAttribute()).toString();
    }
    catch (MalformedURLException e) {
      throw new WebParsingError(this, e);
    }
  }

  public WebPage click() throws WebCommandFailed {
    return browser.doClick(node);
  }
  
  public Download clickAndDownload() {
    return new Download(browser, node);
  }

  public String toString() {
    return node.asText();
  }
}
