package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

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
    dialog.getComboBox("bankCombo:" + entityId).select(bankName);
    return this;
  }

  public void validate() {
    dialog.getButton("ok").click();
    assertFalse(dialog.isVisible());
  }

  public BankEntityEditionChecker selectBank(String bank) {
    dialog.getComboBox("bankCombo").select(bank);
    checkBanksEquals("(Select a bank)", "Autre", "AXA Banque", "Banque Populaire", "BNP Paribas",
                     "Caisse d'épargne", "CIC",
                     "Crédit Agricole", "Crédit Mutuel", "ING Direct", "La Poste", "LCL",
                     "Société Générale");
    return this;
  }

  public BankEntityEditionChecker checkBanksEquals(String... banks) {
    assertTrue(dialog.getComboBox("bankCombo").contentEquals(banks));
    return this;
  }
}
