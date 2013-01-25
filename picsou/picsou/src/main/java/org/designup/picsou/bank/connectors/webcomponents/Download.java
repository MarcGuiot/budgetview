package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Download {
  private WebBrowser browser;
  private HtmlElement element;

  Download(WebBrowser browser, HtmlElement element) {
    this.browser = browser;
    this.element = element;
  }

  public File saveAsOfx() throws WebCommandFailed {
    return doSave(".ofx", null, "all accounts");
  }

  public File saveAsQif(final Glob realAccount) throws WebCommandFailed {
    return doSave(".qif",
                  ("! accountId=" + realAccount.get(RealAccount.ID) + "\n").getBytes(),
                  GlobPrinter.toString(realAccount));
  }

  private File doSave(String extension, byte[] prefix, String errorMessage) throws WebCommandFailed {
    WebResponse response = getWebResponse(errorMessage);
    return saveResponseToTempFile(response, extension, prefix, errorMessage);
  }

  static File saveResponseToTempFile(WebResponse response, String extension, byte[] prefix, String errorMessage) throws WebCommandFailed {
    InputStream contentAsStream = getStream(response, errorMessage);
    File file = null;
    try {
      file = File.createTempFile("budgetview_download", extension);
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      if (prefix != null) {
        fileOutputStream.write(prefix);
      }
      Files.copyInUtf8(contentAsStream, response.getContentCharset(), fileOutputStream);
      file.deleteOnExit();
    }
    catch (IOException e) {
      throw new WebCommandFailed(e, "Cannot create temporary file: " +
                                    (file != null ? file.getAbsolutePath() : "?"));
    }
    return file;
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
    Log.write("download " + message);
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
            downloadAttachmentHandler.wait(3000);
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
