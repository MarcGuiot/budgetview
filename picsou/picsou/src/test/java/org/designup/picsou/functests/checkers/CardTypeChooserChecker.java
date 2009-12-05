package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.ComboBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CardTypeChooserChecker extends GuiChecker {
  private Window dialog;

  public CardTypeChooserChecker(Window dialog) {
    this.dialog = dialog;
  }

  public CardTypeChooserChecker checkNoneAreSelected(String accountName) {
    assertThat(getTypeCombo(accountName).selectionEquals(null));
    return this;
  }

  public CardTypeChooserChecker selectDeferredCard(String accountName, int day) {
    getTypeCombo(accountName).select("deferred");

    Panel panel = getPanel(accountName);
    assertFalse(panel.getTextBox("creditMessage").isVisible());

    ComboBox dayCombo = panel.getComboBox("day");
    assertThat(dayCombo.isVisible());
    assertThat(panel.getTextBox("Select the day of debit").isVisible());

    assertThat(dayCombo.selectionEquals("31"));
    dayCombo.select(Integer.toString(day));
    return this;
  }

  public CardTypeChooserChecker selectCreditCard(String accountName) {
    getTypeCombo(accountName).select("credit");

    Panel panel = getPanel(accountName);
    TextBox messageBox = panel.getTextBox("creditMessage");
    assertThat(messageBox.isVisible());
    assertThat(messageBox.textEquals("Credit cards are not managed"));

    assertFalse(panel.getComboBox("day").isVisible());
    return this;
  }

  private ComboBox getTypeCombo(String accountName) {
    return dialog.getComboBox("cardType:" + accountName);
  }

  private Panel getPanel(String accountName) {
    return dialog.getPanel("accountPanel:" + accountName);
  }

  public CardTypeChooserChecker checkValidateEnabled() {
    assertThat(dialog.getButton("OK").isEnabled());
    return this;
  }

  public CardTypeChooserChecker checkValidateDisabled() {
    assertFalse(dialog.getButton("OK").isEnabled());
    return this;
  }

  public void validate(){
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }
}
