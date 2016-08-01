package com.budgetview.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

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

  public CardTypeChooserChecker selectDeferredCard(String accountName) {
    getTypeCombo(accountName).select("Deferred debit card");

    Panel panel = getPanel(accountName);
    TextBox box = panel.getTextBox("message");
    assertThat(box.textContains("Withdrawals from main account should be categorized in 'Other'"));
    return this;
  }

  public CardTypeChooserChecker selectCreditCard(String accountName) {
    getTypeCombo(accountName).select("Credit card");

    Panel panel = getPanel(accountName);
    TextBox messageBox = panel.getTextBox("message");
    assertThat(messageBox.isVisible());
    assertThat(messageBox.textEquals(""));

    checkComponentVisible(panel, JComboBox.class, "day", false);
    return this;
  }

  private ComboBox getTypeCombo(String accountName) {
    return dialog.getComboBox("cardType:" + accountName);
  }

  private Panel getPanel(String accountName) {
    return dialog.getPanel("accountPanel:" + accountName);
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }
}
