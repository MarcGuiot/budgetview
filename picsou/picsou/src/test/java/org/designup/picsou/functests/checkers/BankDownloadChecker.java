package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class BankDownloadChecker extends GuiChecker {
  private Panel panel;

  BankDownloadChecker(Panel panel) {
    this.panel = panel;
  }

  public BankDownloadChecker selectBank(String bankName) {
    panel.getListBox("bankList").select(bankName);
    return this;
  }

  public BankDownloadChecker checkBankList(String... banks) {
    assertThat(panel.getListBox("bankList").contentEquals(banks));
    return this;
  }

  public BankDownloadChecker checkContainsBanks(String... banks) {
    assertThat(panel.getListBox("bankList").contains(banks));
    return this;
  }

  public BankDownloadChecker checkNoBankSelected() {
    assertThat(panel.getListBox("bankList").selectionIsEmpty());
    return this;
  }

  public BankDownloadChecker checkSelectedBank(String bank) {
    assertThat(panel.getListBox("bankList").selectionEquals(bank));
    return this;
  }

  public BankDownloadChecker setFilter(String filter) {
    panel.getTextBox("bankEditor").setText(filter, false);
    return this;
  }

  public BankDownloadChecker checkBankAccessHidden() {
    checkComponentVisible(panel, JButton.class, "openHelp", false);
    return this;
  }

  public BankDownloadChecker checkHelpAvailable(boolean available) {
    UISpecAssert.assertEquals(available, panel.getButton("openHelp").isEnabled());
    return this;
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(panel.getButton("openHelp").triggerClick());
  }

  public OtherBankSynchroChecker openSynchro(ImportDialogChecker checker) {
    Window window = WindowInterceptor.getModalDialog(panel.getButton("synchronize").triggerClick());
    return new OtherBankSynchroChecker(checker, window);
  }

  public OfxSynchoChecker openOfxSynchro(ImportDialogChecker checker) {
    Window window = WindowInterceptor.getModalDialog(panel.getButton("synchronize").triggerClick());
    return new OfxSynchoChecker(checker, window);
  }

}