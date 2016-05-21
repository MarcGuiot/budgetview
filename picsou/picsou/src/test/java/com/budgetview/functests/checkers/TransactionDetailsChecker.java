package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.PopupButton;
import com.budgetview.model.TransactionType;
import com.budgetview.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class TransactionDetailsChecker extends ViewChecker {

  private Panel panel;

  public TransactionDetailsChecker(Window mainWindow) {
    super(mainWindow);
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectCategorization();
      panel = mainWindow.getPanel("transactionDetails");
    }
    return panel;
  }

  public TransactionDetailsChecker checkLabel(String expected) {
    checkValue("userLabel", expected);
    return this;
  }

  public void checkLabelIsNotEditable() {
    assertFalse(getPanel().getTextBox("userLabel").isEditable());
  }

  public TransactionEditionChecker edit() {
    return TransactionEditionChecker.open(openActionPopup().triggerClick("edit"));
  }

  private void checkValue(String name, String label) {
    assertThat(getPanel().getTextBox(name).textEquals(label));
  }

  public void checkActionsHidden() {
    checkComponentVisible(getPanel(), JButton.class, "transactionActions", false);
  }
  
  private PopupButton openActionPopup() {
    return new PopupButton(getPanel().getButton("transactionActions"));
  }

  public void checkSplitDisabled() {
    openActionPopup().checkItemDisabled("split");
  }

  public void checkSplitEnabled() {
    openActionPopup().checkItemEnabled("split");
  }

  public void checkSplitButtonLabel(String text) {
    openActionPopup().checkContains(text);
  }

  public void split(String amount, String note) {
    SplitDialogChecker splitDialog = openSplitDialog();
    splitDialog.enterAmount(amount);
    splitDialog.enterNote(note);
    splitDialog.validateAndClose();
  }

  public void checkOriginalLabelNotVisible() {
    checkComponentVisible(getPanel(), JLabel.class, "originalLabel", false);
  }

  public void checkOriginalLabel(String originalLabel) {
    assertThat(getPanel().getTextBox("originalLabel").textEquals(originalLabel));
  }

  public void checkType(TransactionType transactionType) {
    TextBox details = getPanel().getTextBox("details");
    assertTrue(details.isVisible());
    assertThat(details.textContains(Lang.get("transactionType." + transactionType.getName())));
  }

  public void checkTypeNotVisible() {
    checkComponentVisible(getPanel(), JLabel.class, "transactionType", false);
  }

  public void checkBankDate(String yyyyMMdd) {
    TextBox details = getPanel().getTextBox("details");
    assertTrue(details.isVisible());
    assertThat(details.textContains("Bank date: " + yyyyMMdd));
  }

  public void checkDetailsNotVisible() {
    checkComponentVisible(getPanel(), JLabel.class, "details", false);
  }

  public void checkBankDateNotVisible() {
    TextBox details = getPanel().getTextBox("details");
    assertFalse(details.textContains("Bank date"));
  }

  public void checkBudgetDate(String yyyyMMdd) {
    TextBox details = getPanel().getTextBox("details");
    assertTrue(details.isVisible());
    assertThat(details.textContains("Budget date: " + yyyyMMdd));
  }

  public void checkBudgetDateNotVisible(String label) {
    TextBox details = getPanel().getTextBox("details");
    assertFalse(details.textContains("Budget date"));
  }

  public void checkAccount(String text) {
    TextBox details = getPanel().getTextBox("details");
    assertThat(details.isVisible());
    assertThat(details.textContains(text));
  }

  public void checkNoAccountDisplayed() {
    checkComponentVisible(getPanel(), JLabel.class, "account", false);
  }

  public SplitDialogChecker openSplitDialog() {
    Window dialog = WindowInterceptor.getModalDialog(openActionPopup().triggerClick("split"));
    return new SplitDialogChecker(dialog);
  }

  public TransactionDetailsChecker checkNote(String text) {
    TextBox textBox = getPanel().getInputTextBox("noteField");
    assertThat(textBox.isVisible());
    assertThat(textBox.textEquals(text));
    return this;
  }

  public TransactionDetailsChecker setNote(String text) {
    getPanel().getInputTextBox("noteField").setText(text);
    return this;
  }

  public void checkNotesFieldNotVisible() {
    checkComponentVisible(getPanel(), JTextField.class, "noteField", false);
  }

  public void checkNothingShown() {
    checkComponentVisible(getPanel(), JPanel.class, "nothingShownPanel", true);
  }

  public void shift() {
    openShiftDialog().validate();
  }

  public ConfirmationDialogChecker openShiftDialog() {
    return ConfirmationDialogChecker.open(openActionPopup().triggerClick("shift"));
  }

  public void checkShiftEnabled() {
    openActionPopup().checkItemEnabled("shift");
  }

  public void checkShiftInverted() {
    openActionPopup().checkItemEnabled("Cancel shift");
  }

  public void checkShiftDisabled() {
    openActionPopup().checkItemDisabled("shift");
  }

  public void unshift() {
    openActionPopup().click("Cancel shift");
  }
}
