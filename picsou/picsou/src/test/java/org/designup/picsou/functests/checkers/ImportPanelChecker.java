package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class ImportPanelChecker extends ViewChecker {
  private Panel panel;

  public ImportPanelChecker(Window window) {
    super(window);
  }

  public ImportDialogChecker openImport() {
    return ImportDialogChecker.open(getPanel().getButton("importButton").triggerClick());
  }

  public OtherBankSynchroChecker openSynchro() {
    ImportDialogChecker importDialogChecker =
      ImportDialogChecker.open(getPanel().getButton("synchroButton").triggerClick());
    return new OtherBankSynchroChecker(importDialogChecker, importDialogChecker.getDialog());
  }

  public ImportPanelChecker checkImportMessage(String message) {
    assertThat(getPanel().getButton("importLabel").textEquals(message));
    return this;
  }

  public ImportPanelChecker checkSynchroMessage(String message) {
    checkComponentVisible(getPanel(), JButton.class, "synchroLabel", true);
    checkComponentVisible(getPanel(), JButton.class, "synchroButton", true);
    assertThat(getPanel().getButton("synchroLabel").textEquals(message));
    return this;
  }

  public void checkSynchroButtonHidden() {
    checkComponentVisible(getPanel(), JButton.class, "synchroLabel", false);
    checkComponentVisible(getPanel(), JButton.class, "synchroButton", false);
  }

  public ImportPanelChecker checkImportSignpostDisplayed(String message) {
    views.selectData();
    checkSignpostVisible(mainWindow, getPanel().getButton("importButton"), message);
    return this;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectData();
      panel = mainWindow.getPanel("importPanel");
    }
    return panel;
  }

  public ImportPanelChecker checkSynchroNotVisible() {
    assertFalse(getPanel().getButton("synchroButton").isVisible());
    return this;
  }

  public ImportPanelChecker checkSynchroVisible() {
    assertTrue(getPanel().getButton("synchroButton").isVisible());
    return this;
  }
}