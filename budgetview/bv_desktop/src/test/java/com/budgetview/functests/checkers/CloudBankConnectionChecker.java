package com.budgetview.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudBankConnectionChecker extends ViewChecker {

  public CloudBankConnectionChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudBankConnectionPanel");
  }

  public CloudBankConnectionChecker setChoice(String label, String item) {
    ComboBox combo = mainWindow.getComboBox(getEditorName(label));
    combo.select(item);
    return this;
  }

  public CloudBankConnectionChecker setText(String label, String text) {
    mainWindow.getTextBox(getEditorName(label)).setText(text);
    return this;
  }

  public CloudBankConnectionChecker setPassword(String label, String password) {
    mainWindow.getPasswordField(getEditorName(label)).setPassword(password);
    return this;
  }

  public CloudBankConnectionChecker setDate(String label, Integer day, String month, Integer year) {
    selectComboValue("dayCombo", day);
    selectComboValue("monthCombo", month);
    selectComboValue("yearCombo", year);
    return this;
  }

  public void selectComboValue(String comboName, Object val) {
    String value = val != null ? val.toString() : "";
    ComboBox combo = mainWindow.getComboBox(comboName);
    combo.select(value);
  }

  public String getEditorName(String label) {
    assertThat(mainWindow.containsLabel(label));
    String labelName = mainWindow.getTextBox(label).getName();
    String id = labelName.substring(labelName.indexOf(":"));
    return "editor" + id;
  }

  public CloudBankConnectionChecker enterAndGetStep2() {
    mainWindow.getButton("next").click();
    return new CloudBankConnectionChecker(mainWindow);
  }

  public CloudFirstDownloadChecker next() {
    mainWindow.getButton("next").click();
    return new CloudFirstDownloadChecker(mainWindow);
  }
}
