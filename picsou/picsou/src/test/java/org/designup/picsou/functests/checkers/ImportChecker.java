package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;

public class ImportChecker {
  private Panel panel;
  private ComboBox bankCombo;
  private TextBox fileField;
  private Button importButton;

  public ImportChecker(Panel panel) {
    this.panel = panel;
    bankCombo = panel.getComboBox("bankCombo");
    fileField = panel.getInputTextBox("fileField");
    importButton = panel.getButton("Import");
  }

  public void selectBank(String bank) {
    bankCombo.select(bank);
  }

  public void selectFiles(String... path) {
    StringBuilder builder = new StringBuilder();
    for (String file : path) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file);
    }
    fileField.setText(builder.toString());
  }

  public void startImport() {
    importButton.click();
  }

  public void checkFileContent(Object[][] expected) {
    Table table = panel.getTable();
    UISpecAssert.assertTrue(table.contentEquals(expected));
  }

  public void checkDates(String... dates) {
    ComboBox dateFormatCombo = panel.getComboBox("dateFormatCombo");
    UISpecAssert.assertTrue(dateFormatCombo.contentEquals(dates));
  }

  public void doImport() {
    panel.getButton("OK").click();
  }

  public void checkErrorMessage(String message) {
    UISpecAssert.assertTrue(panel.getTextBox("message").textContains(Lang.get(message)));
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
      UISpecAssert.assertTrue(fileField.textContains(file));
    }
  }

  public void back() {
    panel.getButton("back").click();
  }
}
