package org.designup.picsou.functests.banks;

public class CaisseEpargneTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importQifFile(10., getFile("caisse_epargne.qif"), "Caisse d'épargne");
    timeline.selectMonths("2008/07", "2008/08", "2008/09");
    transactions
      .initContent()
      .dumpCode();

  }
}
