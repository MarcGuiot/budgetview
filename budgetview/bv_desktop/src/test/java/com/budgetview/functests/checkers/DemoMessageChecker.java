package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.FooterBannerChecker;
import org.uispec4j.Window;

public class DemoMessageChecker {

  private FooterBannerChecker footerBanner;

  public DemoMessageChecker(Window mainWindow) {
    this.footerBanner = new FooterBannerChecker(mainWindow, "demoMessage");
  }

  public DemoMessageChecker checkVisible() {
    footerBanner.checkVisible("This is a demo account");
    return this;
  }

  public DemoMessageChecker checkHidden() {
    footerBanner.checkHidden();
    return this;
  }

  public void exit() {
    footerBanner.getActionButton().click();
  }
}
