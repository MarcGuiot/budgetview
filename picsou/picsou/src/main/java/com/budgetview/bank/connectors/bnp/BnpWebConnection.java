package com.budgetview.bank.connectors.bnp;

import com.gargoylesoftware.htmlunit.*;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.globsframework.utils.stream.ReplacementInputStreamBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class BnpWebConnection extends HttpWebConnection {

  private final boolean FILTER_JS = true;
  private final String[] JS_TO_INCLUDE = {
    "ia/overview/scripts/overview.js",
    "particulier_portail.js",
    "udc_popups.js",
    "udcext.js",
    "prototype.js"};
  private final String[] TO_EXCLUDE = {
    "Entete_std_et_collabv8.html",
    "UDC12_Rebond_Epargne_Bilan_V2MIB.html",
    "footer_std_UdC_Mib_New.html",
    "banque/portail/particulier/bandeau",
    "common/vide.htm"
  };
  static ReplacementInputStreamBuilder builder;

  static {
    builder = new ReplacementInputStreamBuilder();
    builder.replace("maxlength=\"10\" value=\"\" name=\"ch1\" type=\"text\"&gt;".getBytes(),
                    "<INPUT size=\"10\" maxlength=\"6\" name=\"ch1\" value=\"\" type=\"text\" > ".getBytes());
    builder.replace("maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled &gt;>".getBytes(),
                    "<INPUT size=\"10\" maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled > ".getBytes());
    builder.replace("document.write('<INPUT size=\"10\" ');".getBytes(), " ".getBytes());
    builder.replace("document.write('<INPUT size=\"5\" ');".getBytes(), " ".getBytes());
  }

  public BnpWebConnection(WebClient client) {
    super(client);
  }

  public WebResponse getResponse(WebRequest request) throws IOException {
    URL url = request.getUrl();
    String path = url.getPath();
    WebResponse response = null;
    if (!FILTER_JS || shouldInclude(path)) {
      response = super.getResponse(request);
    }
    else {
      response = new StringWebResponse("", url);
    }

    return response;
  }

  private boolean shouldInclude(String path) {
    if (path.endsWith(".js")) {
      for (String js : JS_TO_INCLUDE) {
        if (path.contains(js)) {
          return true;
        }
      }
      return false;
    }
    for (String content : TO_EXCLUDE) {
      if (path.contains(content)) {
        return false;
      }
    }
    return true;
  }

  protected DownloadedContent downloadResponseBody(final HttpResponse httpResponse) throws IOException {
    final DownloadedContent content = super.downloadResponseBody(httpResponse);
    Header type = httpResponse.getEntity().getContentType();
    if (type.getValue() != null && type.getValue().contains("text/html")) {
      return new DownloadedContent() {
        public InputStream getInputStream() throws IOException {
          return builder.create(content.getInputStream());
        }

        public void cleanUp() {
        }

        public boolean isEmpty() {
          return false;
        }
      };
    }
    else {
      return content;
    }
  }
}
