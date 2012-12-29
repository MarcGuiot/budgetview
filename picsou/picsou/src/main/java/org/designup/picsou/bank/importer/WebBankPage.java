package org.designup.picsou.bank.importer;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.importer.webcomponents.WebBrowser;
import org.designup.picsou.bank.importer.webcomponents.utils.Download;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class WebBankPage extends BankPage {
  protected boolean hasError = false;
  protected WebBrowser browser;

  /** @deprecated Replace with WebBrowser & co */
  protected WebClient client;

  /** @deprecated Replace with WebBrowser & co */
  protected HtmlPage page;


  public WebBankPage(Window parent, Directory directory, GlobRepository repository, Integer bankId) {
    super(parent, directory, repository, bankId);
    this.bankId = bankId;
    this.browser = new WebBrowser(new ErrorAlertHandler());
    this.client = browser.getClient();
  }

  public static BufferedImage getFirstImage(HtmlImage img) {
    try {
      final ImageReader imageReader = img.getImageReader();
      return imageReader.read(0);
    }
    catch (IOException e) {
      throw new RuntimeException("Can not load image " + img.getId());
    }
  }

  protected void loadPage(final String url) throws IOException {
    browser.load(url);
    page = browser.getCurrentHtmlPage();
  }

  protected <T extends HtmlElement> T getElementById(final String id) {
    return browser.getElementById(id);
  }

  protected HtmlAnchor findLink(List<HtmlAnchor> anchors, String ref) {
    for (HtmlAnchor anchor : anchors) {
      if (anchor.getHrefAttribute().contains(ref)) {
        return anchor;
      }
    }
    throw new RuntimeException("Can not find ref '" + ref + "' in :\n" + page.asXml());
  }

  protected File downloadFile(Glob realAccount, HtmlElement anchor) {
    Download download = new Download(browser, anchor);
    return download.saveAsQif(realAccount);
  }

  private class ErrorAlertHandler implements AlertHandler {
    public void handleAlert(Page page, String s) {
      hasError = true;
      MessageDialog.show("bank.error", dialog, directory, "bank.error.msg", s);
    }
  }
}
