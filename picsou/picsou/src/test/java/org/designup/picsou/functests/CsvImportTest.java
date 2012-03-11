package org.designup.picsou.functests;

import org.designup.picsou.functests.banks.SpecificBankTestCase;
import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.CsvBuilder;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

public class CsvImportTest extends SpecificBankTestCase {

  public void testStandardCase() throws Exception {
    String file =
      CsvBuilder.init(this, '\t')
        .addEmpty()
        .add("Col1", "Col2", "Col3")
        .addEmpty()
        .add("08/01/01", "RATP", "-10")
        .add("08/01/02", "AUCHAN", "-100")
        .getFile();

    ImportDialogChecker importDialog = operations.openImportDialog().setFilePath(file);

    importDialog.acceptCsvFile()
      .checkContains("Col1", "Col2", "Col3")
      .checkAvailableTypes("Col1", "Do not import", "User date", "Bank date")
      .setAsBankDate("Col1")
      .checkAvailableTypes("Col2", "Do not import", "Label", "Note", "Envelope name", "Sub-envelope name")
      .setAsLabel("Col2")
      .checkAvailableTypes("Col3", "Do not import", "Amount", "Debit", "Credit")
      .setAsAmount("Col3")
      .validate();

    importDialog
      .checkFileContent(new Object[][]{
        {"08/01/01", "RATP", "-10.00"},
        {"08/01/02", "AUCHAN", "-100.00"}
      })
      .setAccountName("imported")
      .selectDateFormat("Year/Month/Day")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100)
      .completeImport();

    transactions.initAmountContent()
      .add("02/01/2008", "AUCHAN", -100.00, "To categorize", 100.00, 100.00, "imported")
      .add("01/01/2008", "RATP", -10.00, "To categorize", 200.00, 200.00, "imported")
      .check();
  }

  public void testMandatoryFieldsMessage() throws Exception {
    String file =
      CsvBuilder.init(this, '\t')
        .addEmpty()
        .add("Col1", "Col2", "Col3", "Col4")
        .addEmpty()
        .add("08/01/01", "RATP", "-10.00", "200")
        .add("08/01/02", "AUCHAN", "-100.00", "300")
        .getFile();

    ImportDialogChecker importDialog = operations.openImportDialog().setFilePath(file);

    importDialog.acceptCsvFile()
      .checkFieldsError("You must select the fields for: Bank date, Label, Amount.")
      .setAsBankDate("Col1")
      .checkFieldsError("You must select the fields for: Label, Amount.")
      .setAsLabel("Col2")
      .checkFieldsError("You must select the field for Amount.")
      .setAsAmount("Col3")
      .checkFieldsComplete("All mandatory fields are selected.")
      .setAsCredit("Col4")
      .checkFieldsError("You cannot select an Amount and a Credit field at the same time.")
      .setAsDebit("Col4")
      .checkFieldsError("You cannot select an Amount and a Debit field at the same time.")
      .setAsCredit("Col3")
      .checkFieldsComplete("All mandatory fields are selected.")
      .setAsIgnore("Col3")
      .checkFieldsError("You must select the field for Credit.")
      .setAsCredit("Col3")
      .checkFieldsComplete("All mandatory fields are selected.")
      .validate();

    importDialog
      .checkFileContent(new Object[][]{
        {"08/01/01", "RATP", "-200.00"},
        {"08/01/02", "AUCHAN", "-300.00"}
      })
      .setAccountName("imported")
      .selectDateFormat("Year/Month/Day")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100)
      .completeImport();

    transactions.initAmountContent()
      .add("02/01/2008", "AUCHAN", -300.00, "To categorize", 100.00, 100.00, "imported")
      .add("01/01/2008", "RATP", -200.00, "To categorize", 400.00, 400.00, "imported")
      .check();
  }

  public void testDefaultColumnNamesAreAutomaticallyRecognized() throws Exception {
    String file =
      CsvBuilder.init(this, ';')
        .add("Bank date", "Libellé", "Montant", "Note")
        .add("08/01/01", "RATP", "-10.00", "Passe navigo")
        .add("08/01/02", "AUCHAN", "-100.00", "Courses")
        .getFile();

    ImportDialogChecker importDialog = operations.openImportDialog().setFilePath(file);

    importDialog.acceptCsvFile()
      .checkContains("Bank date", "Libellé", "Montant")
      .checkIsBankDate("Bank date")
      .checkIsLabel("Libellé")
      .checkIsAmount("Montant")
      .checkIsNote("Note")
      .validate();

    importDialog
      .checkFileContent(new Object[][]{
        {"08/01/01", "RATP", "-10.00"},
        {"08/01/02", "AUCHAN", "-100.00"}
      })
      .setAccountName("imported")
      .selectDateFormat("Year/Month/Day")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100)
      .completeImport();

    transactions.initContent()
      .add("02/01/2008", TransactionType.PRELEVEMENT, "AUCHAN", "Courses", -100.00)
      .add("01/01/2008", TransactionType.PRELEVEMENT, "RATP", "Passe navigo", -10.00)
      .check();
  }

  public void testCancel() throws Exception {
    String file =
      CsvBuilder.init(this, '\t')
        .add("Date", "Libelle", "montant")
        .add("08/01/01", "RATP", "-10")
        .add("08/01/02", "AUCHAN", "-100")
        .getFile();

    ImportDialogChecker importDialog = operations.openImportDialog().setFilePath(file);

    importDialog.acceptCsvFile()
      .cancel();

    importDialog
      .checkNoErrorMessage()
      .close();
  }

  public void testEmptyFile() throws Exception {
    String file =
      CsvBuilder.init(this, '\t')
        .add("Bank date", "Label", "Amount")
        .getFile();

    operations.openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .checkMessageEmptyFile()
      .close();
  }

  public void testBlankFile() throws Exception {
    String fileName = saveFile("");

    operations.openImportDialog()
      .setFilePath(fileName)
      .acceptFile()
      .checkMessageEmptyFile()
      .close();
  }

  public void testImportWithEnvelopes() throws Exception {
    String file =
      CsvBuilder.init(this, '\t')
        .add("date", "libelle", "montant", "enveloppe")
        .add("08/01/01", "RATP", "-10", "Transport")
        .add("08/01/02", "AUCHAN", "-100", "Groceries")
        .getFile();

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(file);

    importDialog.acceptCsvFile()
      .checkContains("date", "libelle", "montant")
      .setAsBankDate("date")
      .setAsLabel("libelle")
      .setAsAmount("montant")
      .setAsEnvelope("enveloppe")
      .validate();

    importDialog
      .checkFileContent(new Object[][]{
        {"08/01/01", "RATP", "-10.00"},
        {"08/01/02", "AUCHAN", "-100.00"}
      })
      .setAccountName("imported")
      .selectDateFormat("Year/Month/Day")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100);

    importDialog.importSeries()
      .checkContains("Transport", "Groceries")
      .setRecurring("Transport")
      .setRecurring("Groceries")
      .validateAndFinishImport();

    transactions.initAmountContent()
      .add("02/01/2008", "AUCHAN", -100.00, "Groceries", 100.00, 100.00, "imported")
      .add("01/01/2008", "RATP", -10.00, "Transport", 200.00, 200.00, "imported")
      .check();

    // test mapping is kept

    String file2 =
      CsvBuilder.init(this, '\t')
        .add("Date", "Libellé", "Montant", "Enveloppe")
        .add("08/01/04", "ED", "-100", "Groceries")
        .getFile();

    importDialog = operations.openImportDialog().setFilePath(file2);
    importDialog.acceptCsvFile()
      .checkContains("Date", "Libellé", "montant")
      .checkIsBankDate("date")
      .checkIsLabel("Libellé")
      .checkIsAmount("Montant")
      .checkIsEnvelope("Enveloppe")
      .validate();

    importDialog
      .checkFileContent(new Object[][]{
        {"08/01/04", "ED", "-100.00"}
      })
      .selectDateFormat("Year/Month/Day")
      .selectAccount("imported")
      .completeImport();

    transactions.initAmountContent()
      .add("04/01/2008", "ED", -100.00, "Groceries", 0.0, 0.0, "imported")
      .add("02/01/2008", "AUCHAN", -100.00, "Groceries", 100.00, 100.00, "imported")
      .add("01/01/2008", "RATP", -10.00, "Transport", 200.00, 200.00, "imported")
      .check();
  }

  public void testImport() throws Exception {

    String fileName = saveFile(
      "Date d'operation,Date de valeur,Debit,Credit,Libelle,Solde\n" +
      "01/05/2008,01/05/2008,-24.80,,PRLV FINAREF VIE 15515580008302 0501120006,625.35\n" +
      "01/05/2008,01/05/2008,-64.45,,CHEQUE 0650079,436.06\n" +
      "01/06/2008,01/06/2008,-7.57,,PAIEMENT CB 0501 PARIS0003859/ NATURALIA CARTE 41257115,120.46\n" +
      "01/10/2008,01/10/2008,-51.55,,PRLV FREE TELECOM FREE HAUTDEBIT 319609820,916.28\n" +
      "01/10/2008,01/10/2008,-31.25,,PAIEMENT CB 0901 CHATENAY MALA CASINO GENEDIS CARTE 41257115,704.57\n" +
      "01/11/2008,01/10/2008,-327.02,,CIC-ASSURANCES JK4537737 1201099 JK4537737,313.7\n" +
      "01/13/2008,01/13/2008,-0.16,,FRAIS PAIE CB OP 7 00 USD,975.74\n" +
      "03/03/2008,03/05/2008,,30.00,REM CHQ REF10674R04,143.68");

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(fileName);

    importDialog.acceptCsvFile()
      .setAsUserDate("Date d'operation")
      .setAsBankDate("Date de valeur")
      .setAsDebit("Debit")
      .setAsCredit("Credit")
      .setAsLabel("Libelle")
      .validate();

    importDialog
      .setAccountName("imported")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100)
      .completeImport();

    timeline.selectAll();

    transactions.initAmountContent()
      .add("03/03/2008", "REM CHQ REF10674R04", 30.00, "To categorize", 100.00, 100.00, "imported")
      .add("13/01/2008", "FRAIS PAIE CB OP 7 00 USD", -0.16, "To categorize", 70.00, 70.00, "imported")
      .add("11/01/2008", "CIC-ASSURANCES JK4537737 1201099 JK4537737", -327.02, "To categorize", 70.16, 70.16, "imported")
      .add("10/01/2008", "PAIEMENT CB 0901 CHATENAY MALA CASINO GENEDIS CARTE 41257115", -31.25, "To categorize", 397.18, 397.18, "imported")
      .add("10/01/2008", "PRLV FREE TELECOM FREE HAUTDEBIT 319609820", -51.55, "To categorize", 428.43, 428.43, "imported")
      .add("06/01/2008", "PAIEMENT CB 0501 PARIS0003859/ NATURALIA CARTE 41257115", -7.57, "To categorize", 479.98, 479.98, "imported")
      .add("05/01/2008", "CHEQUE 0650079", -64.45, "To categorize", 487.55, 487.55, "imported")
      .add("05/01/2008", "PRLV FINAREF VIE 15515580008302 0501120006", -24.80, "To categorize", 552.00, 552.00, "imported")
      .check();
  }

  public void testImportIngFileWithDoubleQuoteStrings() throws Exception {

    String fileName = saveFile(
      "\"Numéro de compte\";\"Nom du compte\";\"Numéro de mouvement\";\"Date comptable\";\"Date valeur\";\"Montant dans la devise du compte\";\"Devise du compte\";\"Montant dans la devise de contre-valeur\";\"Devise de contre-valeur\";\"Rubriques\";\"Libellé\";\"Détail du mouvement\";\"Remarque\";\"Date de comptabilisation en budget\"\n" +
      "\"363-0432535-52 CVV  EUR\";\"JANNEZ DRABIK\";145;19/03/2010;19/03/2010;-9,10;\"EUR\";-367;\"BEF\";\"\";\"Paiement achat Bancontact/Mister Cash IKEA ANDERLECHT ANDERLECHT, 19/03, 14h23\";\"\";\"\";19/03/2010\n" +
      "\"363-0432535-52 CVV  EUR\";\"JANNEZ DRABIK\";144;19/03/2010;19/03/2010;-12,98;\"EUR\";-524;\"BEF\";\"\";\"Paiement achat Bancontact/Mister Cash IKEA ANDERLECHT ANDERLECHT, 19/03, 14h17\";\"\";\"\";19/03/2010");

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(fileName);

    importDialog.acceptCsvFile()
      .checkAvailableTypes("Date comptable", "Do not import", "User date", "Bank date")
      .setAsUserDate("Date comptable")
      .checkAvailableTypes("Date valeur", "Do not import", "User date", "Bank date")
      .checkIsBankDate("Date valeur")
      .checkAvailableTypes("Montant dans la devise du compte", "Do not import", "Amount", "Debit", "Credit")
      .setAsAmount("Montant dans la devise du compte")
      .checkAvailableTypes("Libellé", "Do not import", "Label", "Note", "Envelope name", "Sub-envelope name")
      .checkIsLabel("Libellé")
      .setAsNote("Détail du mouvement")
      .validate();

    importDialog
      .setAccountName("imported")
      .setMainAccount()
      .selectBank("Autre")
      .setPosition(100)
      .completeImport();

    timeline.selectAll();

    transactions.initAmountContent()
      .add("19/03/2010", "PAIEMENT ACHAT BANCONTACT/MISTER CASH IKEA ANDERLECHT ANDERLECHT, 19/03, 14H17", -12.98, "To categorize", 100.00, 100.00, "imported")
      .add("19/03/2010", "PAIEMENT ACHAT BANCONTACT/MISTER CASH IKEA ANDERLECHT ANDERLECHT, 19/03, 14H23", -9.10, "To categorize", 112.98, 112.98, "imported")
      .check();
  }

  public void testSeparatorSelection() throws Exception {
    String file =
      CsvBuilder.init(this, ';')
        .add("Date:Bank", "Label", "Amount", "Envelope,with:others")
        .add("2008/01/01", "RATP:Navigo,Jan08", "-10,000.00", "Transport")
        .add("2008/01/02", "AUCHAN", "100.00", "groceries")
        .getFile();

    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(file);

    importDialog.acceptCsvFile()
      .checkContains("Date:Bank", "Label", "Amount", "Envelope")
      .setAsBankDate("Date:Bank")
      .checkIsLabel("Label")
      .checkIsAmount("Amount")
      .setAsEnvelope("Envelope,with:others")
      .validate();

    importDialog
      .checkFileContent(new Object[][]{
        {"2008/01/02", "AUCHAN", "100.00"},
        {"2008/01/01", "RATP:Navigo,Jan08", "-10000.00"}
      })
      .setAccountName("imported")
      .setMainAccount()
      .selectBank("CIC")
      .setPosition(100);

    importDialog.importSeries()
      .checkContains("Transport", "Groceries")
      .setRecurring("Transport")
      .setRecurring("Groceries")
      .validateAndFinishImport();
  }

  public void testNoSeparatorFound() throws Exception {
    String fileName = saveFile("Blah;,:\tblah");

    operations.openImportDialog()
      .setFilePath(fileName)
      .acceptFile()
      .checkHtmlErrorMessage("import.csv.invalidFileFormat")
      .close();
  }

  private String saveFile(String content) {
    String fileName = TestUtils.getFileName(this, ".csv");
    Files.dumpStringToFile(fileName, content);
    return fileName;
  }
}
