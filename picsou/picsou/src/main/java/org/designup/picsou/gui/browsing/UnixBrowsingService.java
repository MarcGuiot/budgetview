package org.designup.picsou.gui.browsing;

import java.io.IOException;

public class UnixBrowsingService extends BrowsingService {
  public void launchBrowser(String url) {
    try {
      Process p = Runtime.getRuntime().exec("firefox " + url);
      p.getErrorStream().close();
      p.getOutputStream().close();
      p.getInputStream().close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}