package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.Download;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.io.IOException;

public class WebForm extends WebContainer<HtmlForm> {

  public WebForm(WebBrowser browser, HtmlForm form) {
    super(browser, form);
  }

  public void setHiddenFieldById(String id, String value) throws WebParsingError {
    HtmlHiddenInput input = (HtmlHiddenInput)HtmlUnit.getElementById(node, id, HtmlHiddenInput.class);
    input.setAttribute("value", value);
  }

  public WebPage submit() throws WebCommandFailed, WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "type", "submit");
    return doSubmit(input);
  }

  public WebPage submitByName(String name) throws WebParsingError, WebCommandFailed {
    HtmlInput input = (HtmlInput)getElementByName("input", name, HtmlSubmitInput.class);
    return doSubmit(input);
  }

  private WebPage doSubmit(HtmlInput input) throws WebCommandFailed {
    try {
      return browser.setCurrentPage(input.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public Download submitByNameAndDownload(String name) throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "name", name);
    return new Download(browser, input);
  }

  public Download submitAndDownload() throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "type", "submit");
    return new Download(browser, input);
  }
}
