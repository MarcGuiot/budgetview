package com.budgetview.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

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

  public String getEditorName(String label) {
    String labelName = mainWindow.getTextBox(label).getName();
    String id = labelName.substring(labelName.indexOf(":"));
    return "editor" + id;
  }

  public ImportDialogPreviewChecker next() {
    mainWindow.getButton("next").click();
    assertThat(new Assertion() {
      public void check() {
        if (!mainWindow.getPanel("importPreviewPanel").isVisible().isTrue()) {
          UISpecAssert.fail();
        }
      }
    }, 10000);
    return new ImportDialogPreviewChecker(mainWindow);
  }
}
