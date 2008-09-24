package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ImportChecker {
  private Panel panel;
  private TextBox fileField;
  private Button importButton;

  public ImportChecker(Panel panel) {
    this.panel = panel;
    fileField = panel.getInputTextBox("fileField");
    importButton = panel.getButton("Import");
  }

  public void selectBank(String bank) {
    ComboBox accountBankCombo = panel.getComboBox("accountBank");
    accountBankCombo.select(bank);
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

  public void enterAccountNumber(String number) {
    panel.getInputTextBox("number").setText(number);
  }

  public void checkSelectedFiles(String... files) {
    TextBox fileField = panel.getTextBox("fileField");
    for (String file : files) {
      assertTrue(fileField.textContains(file));
    }
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
