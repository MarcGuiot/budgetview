package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class CardTypeChooserChecker extends GuiChecker {
  private Window dialog;

  public CardTypeChooserChecker(Window dialog) {
    this.dialog = dialog;
  }

  public CardTypeChooserChecker checkNoneAreSelected() {
    assertFalse(dialog.getRadioButton("deferred").isSelected());
    assertFalse(dialog.getRadioButton("credit").isSelected());
    return this;
  }

  public CardTypeChooserChecker selectCreditCard(int day) {
    dialog.getRadioButton("credit").click();
    return this;
  }

  public CardTypeChooserChecker selectDeferredCard() {
    dialog.getRadioButton("deferred").click();
    return this;
  }

  public void validate(){
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }
}
