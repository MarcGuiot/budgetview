package org.designup.picsou.functests;

import org.designup.picsou.functests.banks.SpecificBankTestCase;
import org.designup.picsou.functests.checkers.ImportCsvChecker;
import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.checkers.ImportSeriesChecker;
import org.designup.picsou.utils.CsvBuilder;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

public class CsvImportTest extends SpecificBankTestCase {

  public void test() throws Exception {
    String file =
      new CsvBuilder(this, "\t")
        .add("Date", "Libelle", "montant")
        .add("08/01/01", "RATP", "-10")
        .add("08/01/02", "AUCHAN", "-100")
        .getFile();

    ImportDialogChecker importDialogChecker = operations.openImportDialog()
      .setFilePath(file);
    ImportCsvChecker checker = importDialogChecker.acceptCsvFile();
    checker
      .checkSeparator('\t')
      .checkContains("Date", "Libelle", "montant")
      .setAsOperationUserDate("Date")
      .setAsLabel("Libelle")
      .setAsAmount("montant")
      .validate();

    importDialogChecker
      .checkFileContent(new Object[][]{
        {"08/01/01", "RATP", "-10.00"},
        {"08/01/02", "AUCHAN", "-100.00"}
      })
      .setAccountName("imported")
      .selectDate("Year/Month/Day")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100)
      .completeImport();

    transactions.initAmountContent()
      .add("02/01/2008", "AUCHAN", -100.00, "To categorize", 100.00, 100.00, "imported")
      .add("01/01/2008", "RATP", -10.00, "To categorize", 200.00, 200.00, "imported")
      .check();
  }

  public void testCancel() throws Exception {
    String file =
      new CsvBuilder(this, "\t")
        .add("Date", "Libelle", "montant")
        .add("08/01/01", "RATP", "-10")
        .add("08/01/02", "AUCHAN", "-100")
        .getFile();

    ImportDialogChecker importDialogChecker = operations.openImportDialog()
      .setFilePath(file);
    ImportCsvChecker checker = importDialogChecker.acceptCsvFile();
    checker.cancel();
    importDialogChecker.close();
  }


  public void testImportWithEnvelop() throws Exception {
    String file =
      new CsvBuilder(this, "\t")
        .add("Date", "Libelle", "montant", "envelop")
        .add("08/01/01", "RATP", "-10", "transport")
        .add("08/01/02", "AUCHAN", "-100", "course")
        .getFile();

    ImportDialogChecker importDialogChecker = operations.openImportDialog()
      .setFilePath(file);
    ImportCsvChecker checker = importDialogChecker.acceptCsvFile();
    checker.checkContains("Date", "Libelle", "montant")
      .setAsOperationUserDate("date")
      .setAsLabel("Libelle")
      .setAsAmount("montant")
      .setAsEnvelop("envelop")
      .validate();

    importDialogChecker
      .checkFileContent(new Object[][]{
        {"08/01/01", "RATP", "-10.00"},
        {"08/01/02", "AUCHAN", "-100.00"}
      })
      .setAccountName("imported")
      .selectDate("Year/Month/Day")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100);
    ImportSeriesChecker seriesChecker = importDialogChecker.importSeries();
    seriesChecker.checkContains("transport", "course")
      .setRecurring("transport")
      .setRecurring("course")
      .validateAndFinishImport();

    transactions.initAmountContent()
      .add("02/01/2008", "AUCHAN", -100.00, "course", 100.00, 100.00, "imported")
      .add("01/01/2008", "RATP", -10.00, "transport", 200.00, 200.00, "imported")
      .check();
  }

  public void testImport() throws Exception {
    String content =
      "Date d'operation,Date de valeur,Debit,Credit,Libelle,Solde\n" +
      "01/05/2008,01/05/2008,-24.80,,PRLV FINAREF VIE 15515580008302 0501120006,625.35\n" +
      "01/05/2008,01/05/2008,-64.45,,CHEQUE 0650079,436.06\n" +
      "01/05/2008,01/05/2008,-85.00,,CHEQUE 0650080,351.06\n" +
      "01/06/2008,01/06/2008,-60.64,,PAIEMENT CB 0401 LA VILLE DU BO CARREFOURDACLVDB CARTE 41257115,290.42\n" +
      "01/06/2008,01/06/2008,-26.76,,PAIEMENT CB 0401 LA VILLE DU BO CARREFOUR LVDB CARTE 41257115,263.66\n" +
      "01/06/2008,01/06/2008,-38.55,,PAIEMENT CB 0501 VELIZY VILLACO AUCHAN VELIZY CARTE 41257115,133.02\n" +
      "01/06/2008,01/06/2008,-4.99,,PAIEMENT CB 0401 LU LUXEMBOURG APPLE ITUNES STO CARTE 41257115,128.03\n" +
      "01/06/2008,01/06/2008,-7.57,,PAIEMENT CB 0501 PARIS0003859/ NATURALIA CARTE 41257115,120.46\n" +
      "01/10/2008,01/10/2008,-51.55,,PRLV FREE TELECOM FREE HAUTDEBIT 319609820,916.28\n" +
      "01/10/2008,01/10/2008,-31.25,,PAIEMENT CB 0901 CHATENAY MALA CASINO GENEDIS CARTE 41257115,704.57\n" +
      "01/11/2008,01/10/2008,-327.02,,CIC-ASSURANCES JK4537737 1201099 JK4537737,313.7\n" +
      "01/13/2008,01/13/2008,-0.16,,FRAIS PAIE CB OP 7 00 USD,975.74\n" +
      "03/03/2008,03/05/2008,,30.00,REM CHQ REF10674R04,143.68";

    String fileName = TestUtils.getFileName(this, ".csv");
    Files.dumpStringToFile(fileName, content);

    ImportDialogChecker importDialogChecker = operations.openImportDialog()
      .setFilePath(fileName);
    ImportCsvChecker checker = importDialogChecker.acceptCsvFile();
    checker
      .checkSeparator(',')
      .setSeparator(':')
      .checkLine("Date d'operation,Date de valeur,Debit,Cre")
      .setSeparator(',')
      .setAsOperationUserDate("Date d'operation")
      .setAsOperationBankDate("Date de valeur")
      .setAsDebit("Debit")
      .setAsCredit("Credit")
      .setAsLabel("Libelle")
      .validate();

    importDialogChecker
      .setAccountName("imported")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100)
      .completeImport();

    timeline.selectAll();

    transactions.initAmountContent()
      .add("05/03/2008", "REM CHQ REF10674R04", 30.00, "To categorize", 100.00, 100.00, "imported")
      .add("13/01/2008", "FRAIS PAIE CB OP 7 00 USD", -0.16, "To categorize", 70.00, 70.00, "imported")
      .add("10/01/2008", "CIC-ASSURANCES JK4537737 1201099 JK4537737", -327.02, "To categorize", 70.16, 70.16, "imported")
      .add("10/01/2008", "PAIEMENT CB 0901 CHATENAY MALA CASINO GENEDIS CARTE 41257115", -31.25, "To categorize", 397.18, 397.18, "imported")
      .add("10/01/2008", "PRLV FREE TELECOM FREE HAUTDEBIT 319609820", -51.55, "To categorize", 428.43, 428.43, "imported")
      .add("06/01/2008", "PAIEMENT CB 0501 PARIS0003859/ NATURALIA CARTE 41257115", -7.57, "To categorize", 479.98, 479.98, "imported")
      .add("06/01/2008", "PAIEMENT CB 0401 LU LUXEMBOURG APPLE ITUNES STO CARTE 41257115", -4.99, "To categorize", 487.55, 487.55, "imported")
      .add("06/01/2008", "PAIEMENT CB 0501 VELIZY VILLACO AUCHAN VELIZY CARTE 41257115", -38.55, "To categorize", 492.54, 492.54, "imported")
      .add("06/01/2008", "PAIEMENT CB 0401 LA VILLE DU BO CARREFOUR LVDB CARTE 41257115", -26.76, "To categorize", 531.09, 531.09, "imported")
      .add("06/01/2008", "PAIEMENT CB 0401 LA VILLE DU BO CARREFOURDACLVDB CARTE 41257115", -60.64, "To categorize", 557.85, 557.85, "imported")
      .add("05/01/2008", "CHEQUE 0650080", -85.00, "To categorize", 618.49, 618.49, "imported")
      .add("05/01/2008", "CHEQUE 0650079", -64.45, "To categorize", 703.49, 703.49, "imported")
      .add("05/01/2008", "PRLV FINAREF VIE 15515580008302 0501120006", -24.80, "To categorize", 767.94, 767.94, "imported")
      .check();

  }

}
