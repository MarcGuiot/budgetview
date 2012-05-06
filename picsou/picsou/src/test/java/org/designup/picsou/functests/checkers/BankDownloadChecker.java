package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class BankDownloadChecker extends BankChooserPanelChecker<BankDownloadChecker> {

  public BankDownloadChecker(Window dialog) {
    super(dialog);
  }

  public BankDownloadChecker checkManualDownloadHidden() {
    checkComponentVisible(panel, JButton.class, "gotoManualDownload", false);
    return this;
  }

  public BankDownloadChecker checkManualDownloadAvailable() {
    Button button = panel.getButton("gotoManualDownload");
    UISpecAssert.assertThat(button.isEnabled());
    UISpecAssert.assertThat(button.textEquals("Download"));
    return this;
  }

  public BankDownloadChecker selectManualDownload() {
    panel.getButton("gotoManualDownload").click();
    return this;
  }

  public BankDownloadChecker goBackToBankSelection() {
    panel.getButton("gotoBankSelection").click();
    return this;
  }

  public BankDownloadChecker checkManualDownloadHelp(String... chunks) {
    TextBox manualDownloadMessage = panel.getTextBox("manualDownloadMessage");
    for (String chunk : chunks) {
      assertThat(manualDownloadMessage.textContains(chunk));
    }
    return this;
  }

  public OtherBankSynchroChecker openSynchro(ImportDialogChecker checker) {
    Window window = WindowInterceptor.getModalDialog(panel.getButton("synchronize").triggerClick());
    return new OtherBankSynchroChecker(checker, window);
  }

  public OfxSynchoChecker openOfxSynchro(ImportDialogChecker checker) {
    Window window = WindowInterceptor.getModalDialog(panel.getButton("synchronize").triggerClick());
    return new OfxSynchoChecker(checker, window);
  }

  public BankDownloadChecker checkSecurityMessage(String content) {
    Button button = panel.getButton("securityInfo");
    UISpecAssert.assertThat(button.tooltipContains(content));
    return this;
  }

  public void enterTransactionsManually() {
    panel.getTextBox("manualInput").clickOnHyperlink("enter transactions manually");
    assertFalse(panel.isVisible());
  }
}