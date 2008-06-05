package org.designup.picsou.functests.banks;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class CATest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("ca1.ofx"));
    periods.selectCells(0, 1, 2);
    transactions
      .initContent()
      .add("14/08/2006", TransactionType.PRELEVEMENT, "Prel 459654 Free Telecom", "", -45.78)
      .add("11/08/2006", TransactionType.BANK_FEES, "INTERETS DEBITEURS", "", -1.23, MasterCategory.BANK)
      .add("11/08/2006", TransactionType.CREDIT_CARD, "Websncf", "", -162.80)
      .add("09/08/2006", TransactionType.DEPOSIT, "6125145", "", 6828.50)
      .add("07/08/2006", TransactionType.CHECK, "0000119", "", -600.00)
      .add("06/08/2006", TransactionType.CREDIT_CARD, "Grosbill", "", -420.90)
      .add("04/08/2006", TransactionType.VIREMENT, "VIR. PERMANENT", "", -238.00)
      .add("02/08/2006", TransactionType.PRELEVEMENT, "Rbt Adi 08-2006/prets Contrat D", "", -2.32)
      .add("01/08/2006", TransactionType.PRELEVEMENT, "*cotis Csca Pro Non Soumis A Tva", "", -17.34)
      .add("01/08/2006", TransactionType.WITHDRAWAL, "NIMES COUPOLE  31/07 162", "", -100.00)
      .add("31/07/2006", TransactionType.CREDIT, "PRET 610458019PR", "", -137.77)
      .add("21/07/2006", TransactionType.PRELEVEMENT, "Tip France Telecom Montpelli0023", "", -40.19)
      .add("13/07/2006", TransactionType.DEPOSIT, "6125144", "", 3064.60)
      .add("30/06/2006", TransactionType.CREDIT, "PRET 610458019PR", "", -137.77)
      .check();
  }
}
