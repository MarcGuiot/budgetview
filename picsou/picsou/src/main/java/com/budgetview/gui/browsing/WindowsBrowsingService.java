package com.budgetview.gui.browsing;

public class WindowsBrowsingService extends BrowsingService {
  public void launchBrowser(String url) {
    try {
      String cmd = "rundll32" + " " + "url.dll,FileProtocolHandler" + " " + url;
      Process p = Runtime.getRuntime().exec(cmd);
      p.getErrorStream().close();
      p.getOutputStream().close();
      p.getInputStream().close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}