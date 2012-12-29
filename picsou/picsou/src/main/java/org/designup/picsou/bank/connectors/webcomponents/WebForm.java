package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.designup.picsou.bank.connectors.webcomponents.utils.Download;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebForm extends WebPanel {
  private HtmlForm form;

  public WebForm(WebBrowser browser, HtmlForm form) {
    super(browser, form);
    this.form = form;
  }

  public WebPage submit() {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(form, "input", "type", "submit");
    return doSubmit(input);
  }

  public WebPage submitByName(String name) {
    HtmlInput input = (HtmlInput)getElementByName("input", name, HtmlSubmitInput.class);
    return doSubmit(input);
  }

  private WebPage doSubmit(HtmlInput input) {
    try {
      return browser.setCurrentPage(input.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public Download submitByNameAndDownload(String name) {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(form, "input", "name", name);
    return new Download(browser, input);
  }

  public Download submitAndDownload() {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(form, "input", "type", "submit");
    return new Download(browser, input);
  }
}
