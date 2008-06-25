package org.designup.picsou.gui.browsing;

import org.designup.picsou.gui.utils.Gui;

public abstract class BrowsingService {

  static public BrowsingService createService() {
    if (Gui.isLinux()) {
      return new LinuxBrowsingService();
    }
    if (Gui.isMacOSX()) {
      return new MacBrowsingService();
    }
    return new WindowsBrowsingService();
  }

  abstract public void launchBrowser(String url);
}
