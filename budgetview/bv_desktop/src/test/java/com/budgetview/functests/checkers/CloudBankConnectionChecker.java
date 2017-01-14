package com.budgetview.functests.checkers;

import org.junit.Assert;
import org.uispec4j.ComboBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;

import javax.swing.*;

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
    Panel editorPanel = mainWindow.getPanel(getEditorName(label));
    selectComboValue(editorPanel, "dayCombo", day);
    selectComboValue(editorPanel, "monthCombo", month);
    selectComboValue(editorPanel, "yearCombo", year);
    return this;
  }

  public void selectComboValue(Panel editorPanel, String comboName, Object val) {
    String value = val != null ? val.toString() : "";
    ComboBox combo = editorPanel.getComboBox(comboName);
    combo.select(value);
  }

  public String getEditorName(String label) {
    assertThat(mainWindow.containsLabel(label));
    String labelName = mainWindow.getTextBox(label).getName();
    return "editor:" + labelName.substring(labelName.indexOf(":") + 1);
  }

  public CloudBankConnectionChecker checkNoErrorShown() {
    checkComponentVisible(mainWindow, JEditorPane.class, "errorMessage", false);
    return this;
  }

  public CloudFirstDownloadChecker next() {
    mainWindow.getButton("next").click();
    assertThat(new Assertion() {
      public void check() {
        JPanel panel = mainWindow.findSwingComponent(JPanel.class, "importCloudFirstDownloadPanel");
        if (panel == null) {
          JEditorPane errorMessage = mainWindow.findSwingComponent(JEditorPane.class, "errorMessage");
          if (errorMessage != null && errorMessage.isVisible()) {
            Assert.fail("Error message unexpectedly shown: " + errorMessage.getText());
          }
          else {
            Assert.fail("Login failed - showing:\n" + mainWindow.getDescription());
          }
        }
      }
    });
    return new CloudFirstDownloadChecker(mainWindow);
  }

  public CloudBankConnectionChecker nextAndCheckError(String errorText) {
    mainWindow.getButton("next").click();
    TextBox errorLabel = mainWindow.getTextBox("errorMessage");
    assertThat(errorLabel.isVisible());
    assertThat(errorLabel.textContains(errorText));
    checkPanelShown("importCloudBankConnectionPanel");
    return this;
  }

  public CloudBankConnectionChecker nextAndGetStep2() {
    mainWindow.getButton("next").click();
    return new CloudBankConnectionChecker(mainWindow);
  }
}
