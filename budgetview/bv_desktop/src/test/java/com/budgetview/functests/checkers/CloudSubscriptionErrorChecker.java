package com.budgetview.functests.checkers;

import org.uispec4j.Window;

public class CloudSubscriptionErrorChecker extends ViewChecker {

  public CloudSubscriptionErrorChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudSubscriptionErrorPanel");
  }

  public void close() {
    
  }
}
