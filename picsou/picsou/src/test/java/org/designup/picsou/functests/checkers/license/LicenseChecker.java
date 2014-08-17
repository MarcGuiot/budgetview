package org.designup.picsou.functests.checkers.license;

import org.designup.picsou.functests.checkers.ViewChecker;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class LicenseChecker extends ViewChecker {
  private Panel panel;

  public LicenseChecker(Window mainWindow) {
    super(mainWindow);
  }

  public LicenseChecker checkPromotionShown() {
    views.selectHome();
    assertThat(getPanel().getPanel("premiumPromotionPanel").isVisible());
    assertFalse(getPanel().getPanel("premiumTrialPanel").isVisible());
    assertFalse(getPanel().getPanel("premiumRegisteredPanel").isVisible());
    return this;
  }

  public LicenseChecker checkTrialShown() {
    views.selectHome();
    assertFalse(getPanel().getPanel("premiumPromotionPanel").isVisible());
    assertThat(getPanel().getPanel("premiumTrialPanel").isVisible());
    assertFalse(getPanel().getPanel("premiumRegisteredPanel").isVisible());
    return this;
  }

  public LicenseChecker checkRegisteredShown() {
    views.selectHome();
    assertFalse(getPanel().getPanel("premiumPromotionPanel").isVisible());
    assertFalse(getPanel().getPanel("premiumTrialPanel").isVisible());
    assertThat(getPanel().getPanel("premiumRegisteredPanel").isVisible());
    return this;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("dashboardView");
    }
    return panel;
  }

  public void activateTrial() {
    getPanel().getPanel("premiumPromotionPanel").getButton("activateTrial").click();
  }

  public void register() {
    LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
  }
}
