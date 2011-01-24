package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Panel;
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
  private Panel parentWindow;

  public AccountPositionEditionChecker(Window window) {
    this.window = window;
  }

  public AccountPositionEditionChecker(Panel parentWindow, String buttonKey) {
    this.parentWindow = parentWindow;
    this.window =  WindowInterceptor.getModalDialog(parentWindow.getButton(Lang.get(buttonKey)).triggerClick());
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
    checkComponentVisible(window, JEditorPane.class, "initialMessage", true);
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public void validateFromImport() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
    if (parentWindow == null){
      Assert.fail("This dialog was not opened from the import dialog");
    }
    ImportDialogChecker.validateAndComplete(-1, -1, -1, parentWindow, "ok");
  }
}
