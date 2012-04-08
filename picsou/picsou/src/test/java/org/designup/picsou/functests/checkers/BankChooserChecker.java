package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class BankChooserChecker extends BankChooserPanelChecker<BankChooserChecker> {

  public static BankChooserChecker open(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new BankChooserChecker(window);
  }

  public BankChooserChecker(Window window) {
    super(window);
  }

  public void cancel() {
    panel.getButton("cancel").click();
    assertFalse(panel.isVisible());
  }

  public BankChooserChecker checkValidateDisabled() {
    assertFalse(panel.getButton("Ok").isEnabled());
    return this;
  }
  
  public void validate() {
    panel.getButton("Ok").click();
    assertFalse(panel.isVisible());
  }
}
