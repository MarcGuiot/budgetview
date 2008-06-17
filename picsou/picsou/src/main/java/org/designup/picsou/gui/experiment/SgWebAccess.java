package org.designup.picsou.gui.experiment;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import java.io.IOException;

public class SgWebAccess {

  public static void main(String[] args) throws IOException {
    final WebClient webClient = new WebClient();
    final HtmlPage page = (HtmlPage)webClient.getPage("https://logitelnet.socgen.com/index.html");
    HtmlTextInput codeClient = (HtmlTextInput)page.getHtmlElementById("codcli");
    codeClient.click();
    codeClient.setNodeValue("12345678");
    System.out.println(page.asXml());
  }
}
