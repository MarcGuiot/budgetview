package org.designup.picsou.functests.checkers.license;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.ViewChecker;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class LicenseChecker extends ViewChecker {
  private Panel panel;
  private TextBox infoMessage;

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

  public void checkInfoMessageHidden() {
    if (getInfoMessageTextBox().isVisible().isTrue()) {
      Assert.fail("Should be hidden. Shown message: " + getInfoMessageTextBox().getText());
    }
  }

  public LicenseChecker checkInfoMessage(String message) {
    TextBox box = getInfoMessageTextBox();
    assertTrue(box.isVisible());
    assertTrue(box.textContains(message));
    return this;
  }

  public LicenseChecker clickLink(String text) {
    getInfoMessageTextBox().clickOnHyperlink(text);
    return this;
  }

  public LicenseExpirationChecker requestNewLicense() {
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getInfoMessageTextBox().clickOnHyperlink("Ask for a new code");
      }
    });
    return new LicenseExpirationChecker(dialog);
  }

  private TextBox getInfoMessageTextBox() {
    if (infoMessage == null) {
      views.selectHome();
      infoMessage = mainWindow.getTextBox("licenseInfoMessage");
    }
    return infoMessage;
  }
}
