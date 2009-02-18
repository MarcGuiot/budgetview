package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class BanquePopulaireTest extends SpecificBankTestCase {

  public void test() throws Exception {
    operations.importOfxFile(getFile("banque_pop.ofx"));
    timeline.selectAll();
    transactions.getTable().getHeader().click(1);

    transactions.initContent()
      .add("13/02/2009", TransactionType.PRELEVEMENT, "VIR MR OU MME XYZ", "", -100.00)
      .add("13/02/2009", TransactionType.PRELEVEMENT, "FT INTERNET ORANGE 115102921 11378168690WFE N.EMETTEUR: 422262", "", -46.33)
      .add("11/02/2009", TransactionType.CHECK, "CHEQUE N.0004228", "", -25.40)
      .add("10/02/2009", TransactionType.DEPOSIT, "REMISE DE 1 CHEQUE", "", 60.00)
      .add("10/02/2009", TransactionType.VIREMENT, "VIREMENT MME  AZE AZE", "", 1000.00)
      .add("10/02/2009", TransactionType.WITHDRAWAL, "RETRAIT GAB LONS SULLY 2 CARTE *123123123 RETRAIT LE 10/02/2009 A 10:24", "", -30.00)
      .add("10/02/2009", TransactionType.PRELEVEMENT, "SOCRAM BANQUE *PRLV 37589392 N.EMETTEUR: 003476", "", -301.10)
      .add("09/02/2009", TransactionType.VIREMENT, "VIR RSI FRANCHE COMTE B0562572C340139XXX X   0109", "", 18.09)
      .add("09/02/2009", TransactionType.VIREMENT, "VIR XXXX XXXX XXXX 01868546      XXXXX0109", "", 507.17)
      .add("05/02/2009", TransactionType.PRELEVEMENT, "COTIS EQUIPAGE HT:      5,69/TVA19,60:     0,13 EQUIPAGE BPBFC   N  5040064222", "", -5.82)
      .add("05/02/2009", TransactionType.PRELEVEMENT, "CREDIPAR CITROEN *100G0473733 32 050209 N.EMETTEUR: 126896", "", -56.34)
      .add("30/01/2009", TransactionType.PRELEVEMENT, "CARTE FACTURETTES CB VOTRE RELEVE ARRETE AU 30/01/09", "", -200.10)
      .add("29/01/2009", TransactionType.PRELEVEMENT, "DROITS DE GARDE 2009 DU COMPTE 123123123123", "", -35.50)
      .add("26/01/2009", TransactionType.PRELEVEMENT, "ECHEANCE PRET DONT CAP     67,52 ASS.    0,00E INT.    14,36 COM.    0,00E", "", -81.88)
      .add("26/01/2009", TransactionType.VIREMENT, "VIR  C.P.A.M LONS 090220000325 090220000325", "", 12.90)
      .check();
  }
}