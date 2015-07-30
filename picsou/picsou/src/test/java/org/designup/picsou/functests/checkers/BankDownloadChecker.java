package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BankDownloadChecker extends BankChooserPanelChecker<BankDownloadChecker> {

  public BankDownloadChecker(Window dialog) {
    super(dialog);
  }

  public BankDownloadChecker checkManualDownloadHidden() {
    checkComponentVisible(window, JButton.class, "gotoManualDownload", false);
    return this;
  }

  public BankDownloadChecker checkManualDownloadAvailable() {
    Button button = window.getButton("gotoManualDownload");
    UISpecAssert.assertThat(button.isEnabled());
    UISpecAssert.assertThat(button.textEquals("Download"));
    return this;
  }

  public BankDownloadChecker selectManualDownload() {
    window.getButton("gotoManualDownload").click();
    return this;
  }

  public BankDownloadChecker goBackToBankSelection() {
    window.getButton("gotoBankSelection").click();
    return this;
  }

  public BankDownloadChecker checkManualDownloadHelp(String... chunks) {
    TextBox manualDownloadMessage = window.getTextBox("manualDownloadMessage");
    for (String chunk : chunks) {
      assertThat(manualDownloadMessage.textContains(chunk));
    }
    return this;
  }

  public OtherBankSynchroChecker openSynchro(ImportDialogChecker checker) {
    window.getButton("synchronize").click();
    return new OtherBankSynchroChecker(checker, window);
  }

  public OfxSynchoChecker openOfxSynchro(ImportDialogChecker checker) {
    this.window.getButton("synchronize").click();
    return new OfxSynchoChecker(checker, checker.getDialog());
  }

  public BankDownloadChecker checkSecurityMessage(String content) {
    Button button = window.getButton("securityInfo");
    UISpecAssert.assertThat(button.tooltipContains(content));
    return this;
  }

  public void enterTransactionsManually() {
    window.getTextBox("manualInput").clickOnHyperlink("enter transactions manually");
    assertFalse(window.isVisible());
  }
}