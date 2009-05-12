package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class CATest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("ca1.ofx"));
    timeline.selectAll();
    transactions.getTable().getHeader().click(1);

    transactions.initContent()
      .add("14/08/2006", TransactionType.PRELEVEMENT, "Prel 459654 Free Telecom Free Hautdebit 44804529", "", -45.78)
      .add("11/08/2006", TransactionType.CREDIT_CARD, "Websncf Lille", "", -162.80)
      .add("11/08/2006", TransactionType.BANK_FEES, "Interets Debiteurs De Juillet 2006", "", -1.23)
      .add("09/08/2006", TransactionType.DEPOSIT, "REMISE CHEQUES  6125145", "", 6828.50)
      .add("07/08/2006", TransactionType.CHECK, "CHEQUE N°0000119", "", -600.00)
      .add("06/08/2006", TransactionType.CREDIT_CARD, "Grosbill Paris", "", -420.90)
      .add("04/08/2006", TransactionType.PRELEVEMENT, "Sarl 2 Ab Audit", "", -238.00)
      .add("02/08/2006", TransactionType.PRELEVEMENT, "Rbt Adi 08-2006/prets Contrat D", "", -2.32)
      .add("01/08/2006", TransactionType.WITHDRAWAL, "RETRAIT NIMES COUPOLE 31/07 162", "", -100.00)
      .add("01/08/2006", TransactionType.PRELEVEMENT, "*cotis Csca Pro Non Soumis A Tva", "", -17.34)
      .add("31/07/2006", TransactionType.CREDIT, "PRET 610458019PR 072006 CAPITAL 120,", "", -137.77)
      .add("21/07/2006", TransactionType.PRELEVEMENT, "Tip France Telecom Montpelli0023 099533m00000000066210660306580", "", -40.19)
      .add("13/07/2006", TransactionType.DEPOSIT, "REMISE CHEQUES  6125144", "", 3064.60)
      .add("30/06/2006", TransactionType.CREDIT, "PRET 610458019PR 062006 CAPITAL 119,", "", -137.77)
      .check();
  }
}
