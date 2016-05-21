package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.FooterBannerChecker;
import org.uispec4j.Window;

public class NewVersionChecker {

  private FooterBannerChecker footerBanner;

  public NewVersionChecker(Window mainWindow) {
    this.footerBanner = new FooterBannerChecker(mainWindow, "newVersionView");
  }

  public NewVersionChecker checkNewVersionShown() {
    footerBanner.checkVisible("new version");
    return this;
  }

  public NewVersionChecker checkNoNewVersionShown() {
    footerBanner.checkHidden();
    return this;
  }

  public void checkLink(String url) {
    BrowsingChecker.checkDisplay(footerBanner.getActionButton(), url);
  }

  public void hide() {
    footerBanner.hide();
  }
}
