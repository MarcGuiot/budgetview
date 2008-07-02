package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
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

  public void labelIsNotEditable() {
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
    assertTrue(getPanel().getPanel("amountPanel").isVisible());
    checkValue("minimumAmount", minAmount);
    checkValue("maximumAmount", maxAmount);
    checkValue("averageAmount", averageAmount);
  }

  public void checkNoAmountStatistics() {
    assertFalse(getPanel().getPanel("amountPanel").isVisible());
  }

  private void checkValue(String name, String label) {
    assertThat(getPanel().getTextBox(name).textEquals(label));
  }

  private void checkNotVisible(String name) {
    assertFalse(getPanel().getTextBox(name).isVisible());
  }

  public void checkToCategorize() {
    Button hyperlink = getPanel().getButton("categoryChooserLink");
    assertThat(hyperlink.textEquals(Lang.get("category.assignement.required")));
    assertTrue(hyperlink.isVisible());
  }

  public void checkManyCategories() {
    Button hyperlink = getPanel().getButton("categoryChooserLink");
    assertThat(hyperlink.textEquals(Lang.get("transaction.details.multicategories")));
  }

  public void checkCategory(MasterCategory category) {
    Button hyperlink = getPanel().getButton("categoryChooserLink");
    assertThat(hyperlink.textEquals(getCategoryName(category)));
  }

  public void categorizeWithLink(MasterCategory category) {
    categorize(category, getPanel().getButton("categoryChooserLink"));
  }

  private void categorize(MasterCategory category, Button button) {
    CategoryChooserChecker categoryChooserDialog =
      new CategoryChooserChecker(WindowInterceptor.getModalDialog(button.triggerClick()));
    categoryChooserDialog.selectCategory(getCategoryName(category));
  }

  public void checkNoCategory() {
    assertFalse(getPanel().getPanel("categoryChooserPanel").isVisible());
  }

  public void checkSplitNotVisible() {
    assertFalse(getPanel().getButton("splitLink").isVisible());
  }

  public void checkSplitVisible() {
    assertTrue(getPanel().getButton("splitLink").isVisible());
  }

  public void split(String amount, String label) {
    Button splitLink = getPanel().getButton("splitLink");
    SplitDialogChecker splitDialogChecker =
      new SplitDialogChecker(WindowInterceptor.getModalDialog(splitLink.triggerClick()));
    splitDialogChecker.enterAmount(amount);
    splitDialogChecker.enterNote(label);
    splitDialogChecker.add();
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

  public SplitDialogChecker openSplitDialog(int row) {
    return new SplitDialogChecker(WindowInterceptor
      .getModalDialog(getPanel().getButton("splitLink").triggerClick()));
  }

  public CategorizationDialogChecker categorize() {
    Button button = getPanel().getButton("Categorize");
    Window dialog = WindowInterceptor.getModalDialog(button.triggerClick());
    return new CategorizationDialogChecker(dialog);
  }

  public void checkSeries(String name) {
    TextBox seriesName = getPanel().getTextBox("transactionSeriesName");
    assertTrue(seriesName.textEquals(name));
  }
}
