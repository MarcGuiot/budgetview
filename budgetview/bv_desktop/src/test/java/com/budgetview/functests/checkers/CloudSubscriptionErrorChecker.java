package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudSubscriptionErrorChecker extends ViewChecker {

  public CloudSubscriptionErrorChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudSubscriptionErrorPanel");
  }

  public CloudSubscriptionErrorChecker checkNoSubscriptionFound(String email) {
    assertThat(mainWindow.getTextBox("messageField").htmlEquals(Lang.get("import.cloud.subscription.nosubscription", email)));
    return this;
  }

  public CloudSubscriptionErrorChecker checkSubscriptionExpired(String email) {
    assertThat(mainWindow.getTextBox("messageField").textEquals(Lang.get("import.cloud.subscription.expired", email)));
    return this;
  }

  public CloudSubscriptionErrorChecker checkValidationTokenExpired(String email) {
    assertThat(mainWindow.getTextBox("messageField").textEquals(Lang.get("import.cloud.subscription.expired", email)));
    return this;
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
