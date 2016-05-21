package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.budgetview.model.RealAccount;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.IOFailure;

import java.io.*;

public class Download {
  private WebBrowser browser;
  private HtmlElement element;

  Download(WebBrowser browser, HtmlElement element) {
    this.browser = browser;
    this.element = element;
  }

  public String readAsOfx() throws WebCommandFailed {
    return doSave(".ofx", null, "all accounts");
  }

  public String readAsQif(final Glob realAccount) throws WebCommandFailed {
    return doSave(".qif",
                  ("! accountId=" + realAccount.get(RealAccount.ID) + "\n").getBytes(),
                  GlobPrinter.toString(realAccount));
  }

  private String doSave(String extension, byte[] prefix, String errorMessage) throws WebCommandFailed {
    WebResponse response = getWebResponse(errorMessage);
    return readResponse(response, extension, prefix, errorMessage);
  }

  static String readResponse(WebResponse response, String extension, byte[] prefix, String errorMessage) throws WebCommandFailed {
    InputStream contentAsStream = getStream(response, errorMessage);
    try {
      OutputStream fileOutputStream = new ByteArrayOutputStream();
      if (prefix != null) {
        fileOutputStream.write(prefix);
      }
      return Files.loadStreamToString(contentAsStream, response.getContentCharset());
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
  }

  private static InputStream getStream(WebResponse response, String message) throws WebCommandFailed {
    InputStream contentAsStream = null;
    try {
      contentAsStream = response.getContentAsStream();
    }
    catch (Exception e) {
      throw new WebCommandFailed(e, "Failed to load " + message);
    }
    return contentAsStream;
  }

  private WebResponse getWebResponse(String message) throws WebCommandFailed {
//    Log.write("download " + message);
    DownloadAttachmentHandler downloadAttachmentHandler = new DownloadAttachmentHandler();
    browser.getClient().setAttachmentHandler(downloadAttachmentHandler);

    try {
      try {
        element.click();
      }
      catch (IOException e) {
        throw new WebCommandFailed(e, "In anchor click");
      }

      synchronized (downloadAttachmentHandler) {
        if (downloadAttachmentHandler.page == null) {
          try {
            downloadAttachmentHandler.wait(10000);
          }
          catch (InterruptedException e1) {
          }
        }
      }
      if (downloadAttachmentHandler.page == null) {
        throw new WebCommandFailed("No download for " + message);
      }

      return downloadAttachmentHandler.page.getWebResponse();
    }
    finally {
      browser.getClient().setAttachmentHandler(null);
    }
  }

  private class DownloadAttachmentHandler implements AttachmentHandler {
    private Page page;

    public void handleAttachment(Page page) {
      synchronized (this) {
        this.page = page;
        notifyAll();
      }
    }
  }
}
