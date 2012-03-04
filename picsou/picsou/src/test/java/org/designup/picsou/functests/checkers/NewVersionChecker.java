package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.FooterBannerChecker;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

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
