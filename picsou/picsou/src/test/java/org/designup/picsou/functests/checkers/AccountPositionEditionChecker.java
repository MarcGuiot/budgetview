package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;

public class AccountPositionEditionChecker extends GuiChecker {
  private Window window;

  public AccountPositionEditionChecker(Window window) {
    this.window = window;
  }

  public AccountPositionEditionChecker setAmount(Double amount) {
    TextBox textBox = window.getInputTextBox("amountField");
    textBox.clear();
    textBox.appendText(Double.toString(amount));
    return this;
  }

  public void setAmountAndEnter(Double amount) {
    window.getInputTextBox("amountField").setText(Double.toString(amount));
    UISpecAssert.assertFalse(window.isVisible());
  }

  public void setAmountAndEnterInImport(final Double amount) {
    WindowInterceptor.init(new Trigger() {
      public void run() throws Exception {
        window.getInputTextBox("amountField").setText(Double.toString(amount));
      }
    }).process(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        return new MessageDialogChecker(window).triggerClose();
      }
    }).run();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public AccountPositionEditionChecker checkOperationLabel(String label) {
    assertThat(window.getTextBox("labelInfo").textEquals(label));
    return this;
  }

  public AccountPositionEditionChecker checkInitialAmountSelected(String text) {
    TextBox textBox = window.getInputTextBox("amountField");
    assertThat(textBox.textEquals(text));

    JTextField textField = (JTextField)textBox.getAwtComponent();
    Assert.assertEquals(text, textField.getSelectedText());

    return this;
  }

  public AccountPositionEditionChecker checkAccountLabel(String accountName) {
    assertThat(window.getTextBox("accountName").textEquals(accountName));
    return this;
  }

  public AccountPositionEditionChecker checkCancelNotAvailable() {
    checkComponentVisible(window, JButton.class, "cancel", false);
    return this;
  }

  public AccountPositionEditionChecker checkEscNotAvailable() {
    pressEsc(window);
    UISpecAssert.assertTrue(window.isVisible());
    return this;
  }

  public AccountPositionEditionChecker checkInitialMessageDisplayed() {
    checkComponentVisible(window, JTextArea.class, "initialMessage", true);
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public void validateFromImport() {
    ImportChecker.validate(-1, -1, window, "ok");
    UISpecAssert.assertFalse(window.isVisible());
  }

  public Trigger triggerValidate() {
    return window.getButton("ok").triggerClick();
  }
}
