package com.budgetview.functests.checkers;

import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.fail;

public class CloudEditionChecker extends ViewChecker {

  public CloudEditionChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudEditionPanel");
  }

  public CloudEditionChecker checkConnections(String... bankNames) {
    assertThat(new Assertion() {
      public void check() {
        org.uispec4j.Panel connectionsPanel = mainWindow.findUIComponent(org.uispec4j.Panel.class, "connections");
        if (connectionsPanel == null) {
          fail("Connections panel not shown");
        }
        Component[] labels = connectionsPanel.getSwingComponents(JLabel.class, "name");
        java.util.List<String> actualNames = new ArrayList<String>();
        for (Component label : labels) {
          actualNames.add(((JLabel) label).getText());
        }
      }
    });
    return this;
  }

  public CloudBankSelectionChecker addConnection() {
    Button button = mainWindow.getButton("addConnection");
    assertThat(button.isEnabled());
    button.click();
    return new CloudBankSelectionChecker(mainWindow);
  }

  public ImportDialogPreviewChecker download() {
    Button button = mainWindow.getButton("download");
    assertThat(button.isEnabled());
    button.click();
    return new ImportDialogPreviewChecker(mainWindow);
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
