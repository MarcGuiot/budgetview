package org.designup.picsou.functests.banks;

public class MoneyExportTest extends SpecificBankTestCase {
  public void testDefaultQifFile() throws Exception {
    operations.importOfxFile(getFile("money_export_standard.qif"), "CIC");
    transactions.initContent()
      .dumpCode();
  }

  public void testStrictQifFile() throws Exception {
    operations.importOfxFile(getFile("money_export_strict.qif"));
    transactions.initContent()
      .dumpCode();
  }
}
