package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowInterceptor;

public class BankEntityEditionChecker extends GuiChecker {
  private Window dialog;

  public BankEntityEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public BankEntityEditionChecker checkAccountsForEntity(String entityId, String[] accounts) {
    assertTrue(dialog.getTextBox("accountNames:" + entityId).textContains(accounts));
    return this;
  }

  public BankEntityEditionChecker selectBankForEntity(String entityId, String bankName) {
    Window bankChooserWindow = WindowInterceptor.getModalDialog(dialog.getButton("bankChooser:" + entityId).triggerClick());
    BankChooserChecker chooserChecker = new BankChooserChecker(bankChooserWindow);
    chooserChecker.selectBank(bankName);
    chooserChecker.validate();
    return this;
  }

  public void validate() {
    dialog.getButton("ok").click();
    assertFalse(dialog.isVisible());
  }

  public BankEntityEditionChecker selectBank(String bank) {
    Window bankChooserWindow = WindowInterceptor.getModalDialog(dialog.getButton("bankChooser").triggerClick());
    BankChooserChecker chooserChecker = new BankChooserChecker(bankChooserWindow);
    chooserChecker.selectBank(bank);
    chooserChecker.checkListContent("Other", "AXA Banque", "Banque Populaire", "BNP Paribas",
                                    "BNPPF", "Caisse d'épargne", "CIC",
                                    "Crédit Agricole", "Crédit Mutuel", "HSBC", "ING Direct", "La Banque Postale",
                                    "LCL",
                                    "Société Générale");
    chooserChecker.validate();
    return this;
  }
}
