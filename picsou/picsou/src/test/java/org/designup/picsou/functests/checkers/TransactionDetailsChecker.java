package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class TransactionDetailsChecker extends DataChecker {
  private Window window;

  public TransactionDetailsChecker(Window window) {
    this.window = window;
  }

  private Panel getPanel() {
    return window.getPanel("transactionDetails");
  }

  public void checkLabel(String expected) {
    checkValue("userLabel", expected);
  }

  public void checkLabelIsNotEditable() {
    assertFalse(getPanel().getTextBox("userLabel").isEditable());
  }

  public void checkDate(String expected) {
    checkValue("userDate", expected);
  }

  public void checkNoDate() {
    checkNotVisible("userDate");
  }

  public void checkAmount(String label, String amount) {
    checkValue("amountLabel", label);
    checkValue("amountValue", amount);
  }

  public void checkNoAmount() {
    checkNotVisible("amountLabel");
    checkNotVisible("amountValue");
  }

  public void checkAmountStatistics(String minAmount,
                                    String maxAmount,
                                    String averageAmount) {
    assertTrue(getPanel().getPanel("multiSelectionPanel").isVisible());
    checkValue("minimumAmount", minAmount);
    checkValue("maximumAmount", maxAmount);
    checkValue("averageAmount", averageAmount);
  }

  public void checkNoAmountStatistics() {
    assertFalse(getPanel().getPanel("multiSelectionPanel").isVisible());
  }

  private void checkValue(String name, String label) {
    assertThat(getPanel().getTextBox(name).textEquals(label));
  }

  private void checkNotVisible(String name) {
    assertFalse(getPanel().getTextBox(name).isVisible());
  }

  public void checkSplitNotVisible() {
    assertFalse(getPanel().getButton("split").isVisible());
  }

  public void checkSplitVisible() {
    assertTrue(getPanel().getButton("split").isVisible());
  }

  public void checkSplitButtonLabel(String text) {
    assertTrue(getPanel().getButton("split").textEquals(text));
  }

  public void split(String amount, String note) {
    Button splitLink = getPanel().getButton("split");
    SplitDialogChecker splitDialogChecker =
      new SplitDialogChecker(WindowInterceptor.getModalDialog(splitLink.triggerClick()));
    splitDialogChecker.enterAmount(amount);
    splitDialogChecker.enterNote(note);
    splitDialogChecker.ok();
  }

  public void checkOriginalLabelNotVisible() {
    assertFalse(getPanel().getTextBox("originalLabel").isVisible());
  }

  public void checkOriginalLabel(String originalLabel) {
    assertThat(getPanel().getTextBox("originalLabel").textEquals(originalLabel));
  }

  public void checkType(TransactionType transactionType) {
    TextBox box = getPanel().getTextBox("transactionType");
    assertTrue(box.isVisible());
    assertThat(box.textEquals(Lang.get("transactionType." + transactionType.getName())));
  }

  public void checkTypeNotVisible() {
    assertFalse(getPanel().getTextBox("transactionType").isVisible());
  }

  public void checkBankDate(String yyyyMMdd) {
    TextBox bankDate = getPanel().getTextBox("bankDate");
    assertTrue(bankDate.isVisible());
    assertThat(bankDate.textEquals(yyyyMMdd));
  }

  public void checkBankDateNotVisible() {
    TextBox bankDate = getPanel().getTextBox("bankDate");
    assertFalse(bankDate.isVisible());
  }

  public SplitDialogChecker openSplitDialog() {
    Window dialog = WindowInterceptor.getModalDialog(getPanel().getButton("splitLink").triggerClick());
    return new SplitDialogChecker(dialog);
  }

  public void checkSplitButtonAvailable() {
    Button splitMessage = getPanel().getButton("splitLink");
    assertTrue(UISpecAssert.and(splitMessage.isVisible(), splitMessage.isEnabled()));
  }

  public void checkNoSelectionLabels(String label, String received, String spent, String total) {
    assertTrue(getPanel().getPanel("noSelectionPanel").isVisible());
    assertTrue(getPanel().getTextBox("noSelectionLabel").textEquals(label));

    checkNoSelectionLabel("noSelectionReceived", received);
    checkNoSelectionLabel("noSelectionSpent", spent);
    checkNoSelectionLabel("noSelectionTotal", total);
  }

  private void checkNoSelectionLabel(String componentName, String expectedText) {
    TextBox label = getPanel().getTextBox(componentName);
    assertEquals(expectedText != null, label.isVisible());
    if (expectedText != null) {
      assertTrue(label.textEquals(expectedText));
    }
  }

  public TransactionDetailsChecker checkNoSelectionPanelHidden() {
    assertFalse(getPanel().getPanel("noSelectionPanel").isVisible());
    return this;
  }

  /**
   * @deprecated
   */
  public TransactionDetailsChecker checkNote(String text) {
//    assertThat(getPanel().getTextBox("note").textEquals(text));
    System.out.println("############# TransactionDetailsChecker.checkNote: " +
                       "A reintegrer quand il y aura les notes dans TransactionDetails " +
                       "#############");
    return this;
  }
}
