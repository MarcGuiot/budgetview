package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
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

  public void checkLabel(String expected) {
    checkValue("userLabel", expected);
  }

  public void checkLabelIsNotEditable() {
    assertFalse(getPanel().getTextBox("userLabel").isEditable());
  }

  public TransactionEditionChecker edit() {
    return TransactionEditionChecker.open(getPanel().getButton("editTransaction").triggerClick());
  }

  private void checkValue(String name, String label) {
    assertThat(getPanel().getTextBox(name).textEquals(label));
  }

  public void checkSplitNotVisible() {
    checkComponentVisible(getPanel(), JButton.class, "split", false);
  }

  public void checkSplitVisible() {
    assertTrue(getPanel().getButton("split").isVisible());
  }

  public void checkSplitButtonLabel(String text) {
    assertTrue(getPanel().getButton("split").textEquals(text));
  }

  public void split(String amount, String note) {
    Button splitLink = getPanel().getButton("split");
    SplitDialogChecker splitDialog =
      new SplitDialogChecker(WindowInterceptor.getModalDialog(splitLink.triggerClick()));
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
    Window dialog = WindowInterceptor.getModalDialog(getPanel().getButton("splitLink").triggerClick());
    return new SplitDialogChecker(dialog);
  }

  public void checkSplitButtonAvailable() {
    Button splitMessage = getPanel().getButton("splitLink");
    assertTrue(UISpecAssert.and(splitMessage.isVisible(), splitMessage.isEnabled()));
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

  public void checkMessage(String message) {
    assertThat(getPanel().getTextBox("noSelectionMessage").textContains(message));
  }

  public void checkNoDataImportedMessage() {
    assertThat(getPanel().getPanel("noData").getTextBox().textContains("No data imported"));
  }

  public void checkNoDataShownMessage() {
    assertThat(getPanel().getPanel("noData").getTextBox().textContains("No data shown"));
  }

  public void shift() {
    openShiftDialog().validate();
  }

  public ConfirmationDialogChecker openShiftDialog() {
    Button button = getPanel().getButton("shift");
    assertThat(button.isVisible());
    assertThat(button.textEquals("Shift..."));
    return ConfirmationDialogChecker.open(button.triggerClick());
  }

  public void checkShiftEnabled() {
    Button button = getPanel().getButton("shift");
    assertThat(button.isVisible());
    assertThat(button.textEquals("Shift..."));
  }

  public void checkShiftInverted() {
    Button button = getPanel().getButton("shift");
    assertThat(button.isVisible());
    assertThat(button.textEquals("Cancel shift"));
  }

  public void checkShiftDisabled() {
    checkComponentVisible(getPanel(), JButton.class, "shift", false);
  }

  public void unshift() {
    Button button = getPanel().getButton("shift");
    assertThat(button.textEquals("Cancel shift"));
    button.click();
  }
}
