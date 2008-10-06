package org.designup.picsou.gui.browsing;

import org.designup.picsou.gui.utils.Gui;

public abstract class BrowsingService {

  static public BrowsingService createService() {

    if (Gui.isMacOSX()) {
      return new MacBrowsingService();
    }
    if (Gui.isWindows()) {
      return new WindowsBrowsingService();
    }
    return new UnixBrowsingService();
  }

  abstract public void launchBrowser(String url);
}
