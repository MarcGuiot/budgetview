package com.budgetview.functests.checkers;

import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class CloudEditionChecker extends ViewChecker {

  public CloudEditionChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudEditionPanel");
  }

  public CloudEditionChecker checkConnections(final String... bankNames) {
    final Panel panel = CloudEditionChecker.this.getConnectionsPanel();
    assertThat(new Assertion() {
      public void check() {
        TestUtils.assertEquals(getConnectionNames(panel), bankNames);
      }
    });
    return this;
  }

  public CloudEditionChecker checkContainsConnection(final String bankName) {
    final Panel panel = CloudEditionChecker.this.getConnectionsPanel();
    assertThat(new Assertion() {
      public void check() {
        TestUtils.assertContains(getConnectionNames(panel), bankName);
      }
    });
    return this;
  }

  public List<String> getConnectionNames(Panel panel) {
    System.out.println("CloudEditionChecker.getConnectionNames: \n" + panel.getDescription());
    Component[] labels = panel.getSwingComponents(JLabel.class, "connectionName");
    List<String> actualNames = new ArrayList<String>();
    for (Component label : labels) {
      String text = ((JLabel) label).getText();
      System.out.println("    -> " + text);
      actualNames.add(text);
    }
    return actualNames;
  }

  public Panel getConnectionsPanel() {
    JPanel connectionsPanel = mainWindow.findSwingComponent(JPanel.class, "connectionsPanel");
    if (connectionsPanel == null) {
      fail("Connections panel not shown - actual content: " + mainWindow.getDescription());
    }
    return new Panel(connectionsPanel);
  }

  public CloudEditionChecker deleteConnection(String bankName) {
    checkContainsConnection(bankName);
    mainWindow.getButton("delete:" + bankName).click();
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
