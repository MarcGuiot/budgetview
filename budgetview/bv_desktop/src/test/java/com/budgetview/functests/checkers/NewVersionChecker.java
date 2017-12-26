package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.FooterBannerChecker;
import org.uispec4j.Window;

public class NewVersionChecker {

  private FooterBannerChecker footer;

  public NewVersionChecker(Window mainWindow) {
    this.footer = new FooterBannerChecker(mainWindow, "newVersionView");
  }

  public NewVersionChecker checkNewVersionShown(String message) {
    footer.checkVisible(message);
    return this;
  }

  public NewVersionChecker checkHidden() {
    footer.checkHidden();
    return this;
  }

  public NewVersionChecker checkLink(String url) {
    BrowsingChecker.checkDisplay(footer.getActionButton(), url);
    return this;
  }

  public NewVersionChecker hide() {
    footer.hide();
    return this;
  }
}
