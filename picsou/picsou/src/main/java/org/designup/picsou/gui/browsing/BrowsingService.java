package org.designup.picsou.gui.browsing;

import org.globsframework.gui.splits.utils.GuiUtils;

public abstract class BrowsingService {

  static public BrowsingService createService() {

    if (GuiUtils.isMacOSX()) {
      return new MacBrowsingService();
    }
    if (GuiUtils.isWindows()) {
      return new WindowsBrowsingService();
    }
    return new UnixBrowsingService();
  }

  abstract public void launchBrowser(String url);
}
