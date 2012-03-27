package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class BankDownloadChecker extends GuiChecker {
  private Window dialog;
  private Panel panel;

  BankDownloadChecker(Window dialog) {
    this.dialog = dialog;
  }

  public Panel getPanel() {
    if (panel == null) {
      panel = dialog.getPanel("bankDownload");
    }
    return panel;
  }

  public BankDownloadChecker selectBank(String bankName) {
    getPanel().getListBox("bankList").select(bankName);
    return this;
  }

  public BankDownloadChecker checkBankList(String... banks) {
    assertThat(getPanel().getListBox("bankList").contentEquals(banks));
    return this;
  }

  public BankDownloadChecker checkContainsBanks(String... banks) {
    assertThat(getPanel().getListBox("bankList").contains(banks));
    return this;
  }

  public BankDownloadChecker checkNoBankSelected() {
    assertThat(getPanel().getListBox("bankList").selectionIsEmpty());
    return this;
  }

  public BankDownloadChecker checkSelectedBank(String bank) {
    assertThat(getPanel().getListBox("bankList").selectionEquals(bank));
    return this;
  }

  public BankDownloadChecker setFilter(String filter) {
    getPanel().getTextBox("bankEditor").setText(filter, false);
    return this;
  }

  public BankDownloadChecker checkBankAccessHidden() {
    checkComponentVisible(panel, JButton.class, "openHelp", false);
    return this;
  }

  public BankDownloadChecker checkHelpAvailable(String buttonLabel) {
    Button button = getPanel().getButton("openHelp");
    UISpecAssert.assertThat(button.isEnabled());
    UISpecAssert.assertThat(button.textEquals(buttonLabel));
    return this;
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(getPanel().getButton("openHelp").triggerClick());
  }

  public OtherBankSynchroChecker openSynchro(ImportDialogChecker checker) {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton("synchronize").triggerClick());
    return new OtherBankSynchroChecker(checker, window);
  }

  public OfxSynchoChecker openOfxSynchro(ImportDialogChecker checker) {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton("synchronize").triggerClick());
    return new OfxSynchoChecker(checker, window);
  }

  public BankDownloadChecker checkSecurityMessage(String content) {
    Button button = getPanel().getButton("securityInfo");
    UISpecAssert.assertThat(button.tooltipContains(content));
    return this;
  }

  public BankEditionDialogChecker addNewBank() {
     return BankEditionDialogChecker.open(getPanel().getButton("addBank").triggerClick());
  }
}