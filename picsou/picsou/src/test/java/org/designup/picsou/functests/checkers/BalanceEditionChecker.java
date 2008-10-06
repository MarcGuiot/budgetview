package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;

public class BalanceEditionChecker extends DataChecker {
  private Window window;

  public BalanceEditionChecker(Window window) {
    this.window = window;
  }

  public BalanceEditionChecker setAmount(Double amount) {
    window.getInputTextBox("amountField").setText(Double.toString(amount));
    return this;
  }

  public BalanceEditionChecker setAmountWithoutEnter(Double amount) {
    TextBox textBox = window.getInputTextBox("amountField");
    textBox.clear();
    textBox.appendText(Double.toString(amount));
    return this;
  }

  public BalanceEditionChecker checkOperationLabel(String label) {
    assertThat(window.getTextBox("labelInfo").textEquals(label));
    return this;
  }

  public BalanceEditionChecker checkInitialAmountSelected(String text) {
    TextBox textBox = window.getInputTextBox("amountField");
    assertThat(textBox.textEquals(text));

    JTextField textField = (JTextField)textBox.getAwtComponent();
    Assert.assertEquals(text, textField.getSelectedText());

    return this;
  }

  public BalanceEditionChecker checkAccountLabel(String accountName) {
    assertThat(window.getTextBox("accountName").textEquals(accountName));
    return this;
  }

  public BalanceEditionChecker checkCancelNotAvailable() {
    checkComponentVisible(window, JButton.class, "cancel", false);
    return this;
  }

  public BalanceEditionChecker checkEscNotAvailable() {
    pressEsc(window);
    UISpecAssert.assertTrue(window.isVisible());
    return this;
  }

  public BalanceEditionChecker checkInitialMessageDisplayed() {
    checkComponentVisible(window, JTextArea.class, "initialMessage", true);
    return this;
  }

  public BalanceEditionChecker checkDialogClosed() {
    UISpecAssert.assertFalse(window.isVisible());
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public Trigger triggerValidate() {
    return window.getButton("ok").triggerClick();
  }
}
