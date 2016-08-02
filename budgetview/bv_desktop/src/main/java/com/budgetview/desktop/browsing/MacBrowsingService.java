package com.budgetview.desktop.browsing;

import com.budgetview.utils.Lang;
import org.globsframework.utils.exceptions.IOFailure;

import java.io.IOException;

public class MacBrowsingService extends BrowsingService {
  public void launchBrowser(String url) {
    try {
      String cmd = "open " + url;
      Process p = Runtime.getRuntime().exec(cmd);
      p.getErrorStream().close();
      p.getOutputStream().close();
      p.getInputStream().close();
    }
    catch (IOException e) {
      throw new IOFailure(Lang.get("browsing.error"), e);
    }
  }
}
