package org.designup.picsou.gui.browsing;

import org.globsframework.gui.splits.utils.GuiUtils;

public abstract class BrowsingService {

  private static boolean useDummy = false;

  public static void setDummyBrowser(boolean useDummy) {
    BrowsingService.useDummy = useDummy;
  }

  public static BrowsingService createService() {

    if (useDummy) {
      return new DummyBrowsingService();
    }
    if (GuiUtils.isMacOSX()) {
      return new MacBrowsingService();
    }
    if (GuiUtils.isWindows()) {
      return new WindowsBrowsingService();
    }
    return new UnixBrowsingService();
  }

   public abstract void launchBrowser(String url);
}
