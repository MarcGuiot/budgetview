package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.FooterBannerChecker;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

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