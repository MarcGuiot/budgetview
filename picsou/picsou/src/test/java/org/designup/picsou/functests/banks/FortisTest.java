package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class FortisTest extends SpecificBankTestCase {

  public void test() throws Exception {
    operations.importOfxFile(getFile("fortis.ofx"));
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("10/12/2009", TransactionType.WITHDRAWAL, "RETRAIT AVEC LA CARTE XXXX 0413 3617 71XX FORTIS RUE NEUVE BRUXELLES 10-12-2009", "", -70.00)
      .add("03/12/2009", TransactionType.PRELEVEMENT, "DOMICILIATION SA MOBISTAR NV COMMUNICATION: 010762102005", "", -31.60)
      .add("03/12/2009", TransactionType.CREDIT_CARD, "PAIEMENT CARTE  XXXX 0413 3617 71XX STIB ROGIER SCHAERBEE 03-12-2009 EXECUTE LE 03-12", "", -24.60)
      .add("03/12/2009", TransactionType.VIREMENT, "VIREMENT AU COMPTE DE SCARLET DE CHEZ TRUC", "", -55.55)
      .add("02/12/2009", TransactionType.VIREMENT, "VIREMENT DU COMPTE DE SOGETI BELGIUM SA AVENUE JULES BORDET 160 1140 EVERE COMMUNICATION: /A/ 1AA3800-04-0001023 SALAIRE", "", 55.55)
      .add("02/12/2009", TransactionType.VIREMENT, "VIREMENT AU COMPTE DE SCARLET VIA PC BANKING  COMMUNICATION:  953101296156", "", -55.55)
      .add("01/12/2009", TransactionType.CREDIT_CARD, "PAIEMENT CARTE  XXXX 0413 3617 71XX QUICK RUE NEUVE 1000 BRUX 01-12-2009", "", -7.25)
      .check();

  }
}
