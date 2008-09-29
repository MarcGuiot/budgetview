package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class CreditMutuelTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importQifFile(getFile("credit_mutuel.qif"), "Crédit Mutuel");
    timeline.selectAll();
    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "MUTUELLE 403 111111", "", -68.25)
      .add("15/07/2008", TransactionType.PRELEVEMENT, "EDF PR QE CLIO BRED 001007", "", -25.00)
      .add("10/07/2008", TransactionType.CHECK, "CHEQUE N. 11111", "", -74.50)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "GROUPAMA 333333", "", -30.78)
      .add("08/07/2008", TransactionType.CREDIT_CARD, "GARAGE YVES", "", -96.70)
      .add("05/07/2008", TransactionType.VIREMENT, "COUTURA", "", 300.00)
      .add("04/07/2008", TransactionType.VIREMENT, "COURAP330332102950TG033", "", 4.94)
      .add("02/07/2008", "03/07/2008", TransactionType.CREDIT_CARD, "AUCHAN CARBURANT CHASSENEUIL DU", "", -53.45)
      .add("01/07/2008", TransactionType.VIREMENT, "SARL CENTRE OUEST EVASIO", "", 150.00)
      .add("01/07/2008", TransactionType.PRELEVEMENT, "CAPMA-CAPMI 102113", "", -109.59)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "URSSAF ST ETIENNE-CN 143065", "", -148.23)
      .add("27/06/2008", TransactionType.VIREMENT, "CAPMA & CAPMI", "", 172.29)
      .add("26/06/2008", TransactionType.VIREMENT, "CNE AVAILLES 24", "", 111.69)
      .add("22/06/2008", TransactionType.VIREMENT, "DOMI LIVRET FIDELITE", "", -1222.33)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "COFINTEX 6 - ACTIVEI 443639", "", -89.94)
      .add("18/06/2008", TransactionType.DEPOSIT, "REMISE CHEQUE 7185464 0513 001 CHQ", "", 1234567.89)
      .add("12/06/2008", TransactionType.DEPOSIT, "REMISE CHEQUE GUICHET", "", 60.00)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "F COTISATION EUROCOMPTE", "", -6.00)
      .add("30/12/2007", "02/01/2008", TransactionType.CREDIT_CARD, "BURTON LIMOGES", "", -104.50)
      .check();
  }
}