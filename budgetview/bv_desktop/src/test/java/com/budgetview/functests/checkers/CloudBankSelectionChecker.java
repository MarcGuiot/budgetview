package com.budgetview.functests.checkers;

import org.uispec4j.ListBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CloudBankSelectionChecker extends ViewChecker {

  public CloudBankSelectionChecker(Window dialog) {
    super(dialog);
    checkPanelShown("importCloudBankSelectionPanel");
  }

  public CloudBankSelectionChecker checkContainsBanks(String... bankNames) {
    ListBox bankList = getBankList();
    assertThat(bankList.contains(bankNames));
    return this;
  }

  public ListBox getBankList() {
    return mainWindow.getPanel("bankChooserPanel").getListBox("bankList");
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }

  public CloudBankSelectionChecker selectBank(String bank) {
    getBankList().select(bank);
    return this;
  }

  public CloudBankSelectionChecker checkBankNotShown(String bank) {
    ListBox bankList = getBankList();
    assertFalse(bankList.contains(bank));
    return this;
  }

  public CloudBankSelectionChecker checkNoBankSelected() {
    assertThat(getBankList().selectionIsEmpty());
    return this;
  }

  public CloudBankSelectionChecker checkNextDisabled() {
    assertFalse(mainWindow.getButton("next").isEnabled());
    return this;
  }

  public CloudBankConnectionChecker next() {
    mainWindow.getButton("next").click();
    return new CloudBankConnectionChecker(mainWindow);
  }
}
