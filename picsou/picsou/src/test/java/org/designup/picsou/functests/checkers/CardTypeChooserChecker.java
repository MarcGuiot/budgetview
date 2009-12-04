package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.ComboBox;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

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


  public CardTypeChooserChecker selectDeferredCard(int day) {
    dialog.getRadioButton("deferred").click();
    ComboBox box = dialog.getComboBox();
    assertThat(dialog.getTextBox("Select the day of debit").isVisible());
    assertThat(box.selectionEquals("31"));
    box.select(Integer.toString(day));
    return this;
  }

  public CardTypeChooserChecker selectCreditCard() {
    dialog.getRadioButton("credit").click();
    assertThat(dialog.getTextBox("creditMessage").textEquals("Credit card are not managed"));
    return this;
  }

  public void validate(){
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }
}
