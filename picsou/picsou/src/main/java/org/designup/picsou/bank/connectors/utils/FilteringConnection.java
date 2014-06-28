package org.designup.picsou.bank.connectors.utils;

import com.gargoylesoftware.htmlunit.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FilteringConnection extends HttpWebConnection {

  public Set<String> toExclude = new HashSet<String>();
  private boolean debugEnabled;

  public FilteringConnection(WebClient webClient) {
    super(webClient);
  }

  public void exclude(String... elements) {
    for (String element : elements) {
      toExclude.add(element);
    }
  }

  public WebResponse getResponse(WebRequest request) throws IOException {
    String url = request.getUrl().toString().toLowerCase();
    for (String path : toExclude) {
      if (url.contains(path.toLowerCase())) {
        return new StringWebResponse("", request.getUrl());
      }
    }
    if (debugEnabled) {
      System.out.print(request.getHttpMethod().toString().toLowerCase() + ": " + url + "...");
    }

    WebResponse response = super.getResponse(request);
    if (debugEnabled) {
      System.out.println(" Done in " + response.getLoadTime() + "ms");
    }
    return response;
  }

  public void setDebugEnabled(boolean debugEnabled) {
    this.debugEnabled = debugEnabled;
  }
}
