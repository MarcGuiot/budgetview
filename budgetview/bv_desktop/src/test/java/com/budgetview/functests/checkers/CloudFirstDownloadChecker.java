package com.budgetview.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudFirstDownloadChecker extends ViewChecker {

  public CloudFirstDownloadChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudFirstDownloadPanel");
  }

  public ImportDialogPreviewChecker waitForNotificationAndDownload(Assertion assertion) {
    assertion.check(); // no repeat to get the right message
    download();
    return new ImportDialogPreviewChecker(mainWindow);
  }

  public CloudFirstDownloadChecker checkNextDisabled() {
    assertFalse(mainWindow.getButton("next").isEnabled());
    return this;
  }

  public CloudFirstDownloadChecker checkNoDataMessageHidden() {
    checkComponentVisible(mainWindow.getPanel("importCloudFirstDownloadPanel"), JLabel.class, "noData", false);
    return this;
  }

  public CloudFirstDownloadChecker checkNoDataMessageShown() {
    checkComponentVisible(mainWindow.getPanel("importCloudFirstDownloadPanel"), JLabel.class, "noData", true);
    return this;
  }

  public CloudFirstDownloadChecker download() {
    mainWindow.getButton("download").click();
    return this;
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
