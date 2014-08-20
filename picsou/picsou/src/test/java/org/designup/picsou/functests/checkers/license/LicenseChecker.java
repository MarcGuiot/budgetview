package org.designup.picsou.functests.checkers.license;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.ViewChecker;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class LicenseChecker extends ViewChecker {
  private TextBox infoMessage;

  public LicenseChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void register() {
    LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
  }

  public LicenseChecker checkInfoMessageHidden() {
    OperationChecker operations = new OperationChecker(mainWindow);
    operations.hideSignposts();
    views.selectHome();
    if (getInfoMessageTextBox().isVisible().isTrue()) {
      Assert.fail("Should be hidden. Shown message: " + getInfoMessageTextBox().getText());
    }
    return this;
  }

  public LicenseChecker checkInfoMessage(String message) {
    OperationChecker operations = new OperationChecker(mainWindow);
    operations.hideSignposts();
    TextBox box = getInfoMessageTextBox();
    assertTrue(box.textContains(message));
    assertTrue(box.isVisible());
    return this;
  }

  public LicenseChecker clickLink(String text) {
    getInfoMessageTextBox().clickOnHyperlink(text);
    return this;
  }

  public LicenseExpirationChecker requestNewLicense() {
    OperationChecker operations = new OperationChecker(mainWindow);
    operations.hideSignposts();
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getInfoMessageTextBox().clickOnHyperlink("Ask for a new code");
      }
    });
    return new LicenseExpirationChecker(dialog);
  }

  public LicenseChecker checkUserIsRegistered() {
    OperationChecker operations = new OperationChecker(mainWindow);
    operations.hideSignposts();
    assertThat(getInfoMessageTextBox().textContains("You are registered"));
    return this;
  }

  public void checkUserNotRegistered() {
    checkInfoMessageHidden();
  }

  private TextBox getInfoMessageTextBox() {
    if (infoMessage == null) {
      views.selectHome();
      infoMessage = mainWindow.getTextBox("licenseInfoMessage");
    }
    return infoMessage;
  }

  public void checkMailKilled(String email) {
    checkInfoMessage("Activation failed. An email was sent at " + email + " with further information.");
  }

  public void checkActivationFailed() {
    TextBox message = getInfoMessageTextBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains("This activation code is not valid."));
    assertTrue(message.textContains("You can request"));
  }

  public void checkKilled() {
    TextBox message = getInfoMessageTextBox();
    assertThat(message.isVisible());
    assertTrue(message.textContains("You are not allowed to import data anymore"));
  }
}
