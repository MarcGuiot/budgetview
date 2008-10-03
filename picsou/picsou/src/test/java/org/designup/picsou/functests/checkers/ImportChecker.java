package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

public class ImportChecker {
  private Panel panel;
  private TextBox fileField;
  private Button importButton;

  public ImportChecker(Panel panel) {
    this.panel = panel;
    fileField = panel.getInputTextBox("fileField");
    importButton = panel.getButton("Import");
  }

  public ImportChecker selectBank(String bank) {
    ComboBox accountBankCombo = panel.getComboBox("accountBank");
    accountBankCombo.select(bank);
    return this;
  }

  public ImportChecker selectFiles(String... path) {
    StringBuilder builder = new StringBuilder();
    for (String file : path) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file);
    }
    fileField.setText(builder.toString());
    return this;
  }

  public ImportChecker startImport() {
    importButton.click();
    return this;
  }

  public void checkFileContent(Object[][] expected) {
    Table table = panel.getTable();
    assertTrue(table.contentEquals(expected));
  }

  public void checkDates(String... dates) {
    ComboBox dateFormatCombo = panel.getComboBox("dateFormatCombo");
    assertTrue(dateFormatCombo.contentEquals(dates));
  }

  public void doImport() {
    panel.getButton("OK").click();
  }

  public BalanceEditionChecker doImportWithBalance() {
    return new BalanceEditionChecker(WindowInterceptor.getModalDialog(panel.getButton("OK").triggerClick()));
  }

  public void close() {
    panel.getButton("close").click();
  }

  public void checkErrorMessage(String message, String... arg) {
    assertTrue(panel.getTextBox("message").textContains(Lang.get(message, arg)));
  }

  public void selectDate(String dateFormat) {
    ComboBox dateFormatCombo = panel.getComboBox("dateFormatCombo");
    dateFormatCombo.select(dateFormat);
  }

  public ImportChecker enterAccountNumber(String number) {
    panel.getInputTextBox("number").setText(number);
    return this;
  }

  public ImportChecker checkSelectedFiles(String... files) {
    TextBox fileField = panel.getTextBox("fileField");
    for (String file : files) {
      assertTrue(fileField.textContains(file));
    }
    return this;
  }

  public void checkCloseButton(String text) {
    assertThat(panel.getButton("close").textEquals(text));
  }

  public void checkSelectedAccount(String accountNumber) {
    assertThat(panel.getComboBox("accountCombo").selectionEquals(accountNumber));
  }

  public void checkNoErrorMessage() {
    TextBox message = (TextBox)panel.findUIComponent(ComponentMatchers.innerNameIdentity("message"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
  }
}
