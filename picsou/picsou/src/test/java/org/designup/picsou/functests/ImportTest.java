package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.ImportChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.uispec4j.*;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.File;

public class ImportTest extends LoggedInFunctionalTestCase {
  protected Window window;
  protected TextBox fileField;
  protected Button importButton;
  protected ComboBox bankCombo;

  protected void setUp() throws Exception {
    super.setUp();
    openImportDialog();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    window.getAwtComponent().setVisible(false);
    window.dispose();
  }

  private void openImportDialog() {
    if (window != null) {
      window.getAwtComponent().setVisible(false);
      window.dispose();
    }
    window = WindowInterceptor.getModalDialog(operations.getImportTrigger());
    bankCombo = window.getComboBox("bankCombo");
    fileField = window.getInputTextBox("fileField");
    importButton = window.getButton("Import");
  }

  public void testCloseBeforeImport() throws Exception {
    ImportChecker importPanel = new ImportChecker(window);
    importPanel.checkCloseButton(Lang.get("import.step1.close"));
  }

  public void testStandardImport() throws Exception {

    bankCombo.select("CIC");
    assertNotNull(window.getButton("http://www.cic.fr/telechargements.cgi"));

    checkLoginMessage("Select an OFX or QIF file to import");

    importButton.click();
    checkErrorMessage("login.data.file.required");

    fileField.setText("blah.ofx");
    importButton.click();
    checkErrorMessage("login.data.file.not.found");

    final String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    WindowInterceptor.init(window.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();

    assertTrue(fileField.textEquals(path));
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "Menu K", "-1.10"}
    }));

    window.getButton("OK").click();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testImportSeveralFiles() throws Exception {
    final String path1 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    final String path2 = OfxBuilder
      .init(this)
      .addTransaction("2006/02/20", -2.2, "Menu K")
      .save();

    fileField.setText(path1 + ";" + path2);
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "Menu K", "-1.10"}
    }));

    window.getButton("OK").click();

    assertTrue(table.contentEquals(new Object[][]{
      {"20/02/2006", "Menu K", "-2.20"}
    }));

    window.getButton("OK").click();

    transactions
      .initContent()
      .add("20/02/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.2)
      .check();
  }

  public void testImportQifFileWithNoExistingAccount() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    bankCombo.select("Societe Generale");
    fileField.setText(path1);
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "Menu K", "-1.10"}
    }));
    ComboBox accountBankCombo = window.getComboBox("accountBank");
    assertThat(accountBankCombo.selectionEquals("Societe Generale"));

    TextBox accountNameField = window.getInputTextBox("name");
    assertThat(accountNameField.textEquals("Main account"));
    accountNameField.setText("My SG account");
    window.getButton("OK").click();
    checkImportMessage("You must enter the account number");
    window.getInputTextBox("number").setText("0123546");

//    window.getInputTextBox("balance").setText("66.50");

    window.getButton("OK").click();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testSkipFirstQifFile() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "First operation")
      .save();
    final String path2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/20", -2.2, "Second operation")
      .save();

    fileField.setText(path1 + ";" + path2);
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "First operation", "-1.10"}
    }));

    window.getButton("OK").click();
    checkImportMessage("You must select the account bank");

    window.getButton("Skip").click();

    checkImportMessage("");

    assertTrue(table.contentEquals(new Object[][]{
      {"20/01/2006", "Second operation", "-2.20"}
    }));

    ComboBox comboBox = window.getComboBox("accountBank");
    comboBox.select("Societe Generale");
    window.getInputTextBox("number").setText("1111");
    window.getButton("OK").click();

    transactions
      .initContent()
      .add("20/01/2006", TransactionType.PRELEVEMENT, "Second operation", "", -2.2)
      .check();
  }

  public void testImportQifFileWithExistingAccount() throws Exception {
    bankCombo.select("Societe Generale");
    String firstQif = QifBuilder.init(this)
      .addTransaction("2006/01/01", 10, "monop")
      .save();
    fileField.setText(firstQif);
    importButton.click();
    window.getButton("OK").click();

    openImportDialog();
    String qifFile = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    fileField.setText(qifFile);
    bankCombo.select("Societe Generale");
    importButton.click();
    ComboBox comboBox = window.getComboBox("accountCombo");
    assertTrue(comboBox.contentEquals("Main account"));
    assertTrue(comboBox.selectionEquals("Main account"));
  }

  public void testImportWithCreateAccountCheckAccountBankIsFilled() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    fileField.setText(path1);
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "Menu K", "-1.10"}
    }));

    assertTrue(window.getComboBox("accountBank").selectionEquals(null));
    window.getInputTextBox("number").setText("0123546");
    window.getButton("OK").click();
    checkImportMessage("You must select the account bank");
  }

  public void testOfxWithUnknownBankEntities() throws Exception {
    String fileName = OfxBuilder.init(this)
      .addBankAccount(666, 1024, "12345678a", 12.0, "2008/06/11")
      .addTransaction("2008/06/10", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .addBankAccount(666, 1024, "12345678b", 12.0, "2008/06/11")
      .addTransaction("2008/06/12", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .addBankAccount(777, 1027, "87654321", 21.0, "2008/06/12")
      .addTransaction("2008/06/10", 10.0, "McDo")
      .addCardAccount("1111222233334444", 7.5, "2008/06/12")
      .addTransaction("2008/06/10", 71.0, "Metro")
      .save();

    fileField.setText(fileName);
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"12/06/2008", "V'lib", "1.00"},
      {"10/06/2008", "Metro", "71.00"},
      {"10/06/2008", "McDo", "10.00"},
      {"10/06/2008", "V'lib", "1.00"},
    }));

    assertEquals(2, window.getSwingComponents(JTextArea.class).length);

    TextBox accountNames0 = window.getTextBox("accountNames0");
    assertTrue(accountNames0.textContains(new String[]{"12345678a", "12345678b"}));

    TextBox accountNames1 = window.getTextBox("accountNames1");
    assertTrue(accountNames1.textContains(new String[]{"1111222233334444", "87654321"}));

    window.getButton("OK").click();
    checkImportMessage("You must associate a bank to each account");

    ComboBox combo0 = window.getComboBox("bankCombo0");
    combo0.select("CIC");

    ComboBox combo1 = window.getComboBox("bankCombo1");
    combo1.select("Societe Generale");

    window.getButton("OK").click();

    String secondFileName = OfxBuilder.init(this)
      .addBankAccount(666, 2048, "77777777", 77.0, "2008/06/11")
      .addTransaction("2008/06/14", 1.0, "V'lib", MasterCategory.TRANSPORTS)
      .save();
    openImportDialog();
    fileField.setText(secondFileName);
    importButton.click();
    window.getButton("OK").click();

    transactions
      .initContent()
      .add("14/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00, MasterCategory.TRANSPORTS)
      .add("12/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00, MasterCategory.TRANSPORTS)
      .add("10/06/2008", TransactionType.VIREMENT, "McDo", "", 10.00)
      .add("10/06/2008", TransactionType.CREDIT_CARD, "Metro", "", 71.00)
      .add("10/06/2008", TransactionType.VIREMENT, "V'lib", "", 1.00, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testSelectDateFormat() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("01/01/01", -1.1, "Menu K")
      .save();

    ImportChecker importPanel = new ImportChecker(window);

    importPanel.selectBank("Societe Generale");
    importPanel.selectFiles(path1);
    importPanel.startImport();

    importPanel.checkFileContent(new Object[][]{
      {"01/01/01", "Menu K", "-1.10"}
    });

    importPanel.checkDates("Year/Month/Day", "Month/Day/Year", "Day/Month/Year");
    importPanel.doImport();
    importPanel.checkErrorMessage("import.dateformat.undefined");

    importPanel.selectDate("Day/Month/Year");
    importPanel.checkFileContent(new Object[][]{
      {"01/01/2001", "Menu K", "-1.10"}
    });

    importPanel.doImport();
    importPanel.enterAccountNumber("0123546");
  }


  public void testBackAtStep2CancelCurrentImport() throws Exception {
    final String path1 = QifBuilder
      .init(this)
      .addTransaction("2001/01/01", -1.1, "Menu K")
      .save();
    final String path2 = QifBuilder
      .init(this)
      .addTransaction("2001/01/02", -10, "Royal cheese")
      .save();

    ImportChecker importPanel = new ImportChecker(window);

    importPanel.selectBank("Societe Generale");
    importPanel.selectFiles(path1, path2);
    importPanel.startImport();

    importPanel.checkFileContent(new Object[][]{
      {"01/01/2001", "Menu K", "-1.10"}
    });
    importPanel.enterAccountNumber("0123546");
    importPanel.doImport();
    importPanel.checkFileContent(new Object[][]{
      {"02/01/2001", "Royal cheese", "-10.00"}
    });

    importPanel.back();

    final String path3 = QifBuilder
      .init(this)
      .addTransaction("2001/01/02", -2.0, "Big Mac")
      .save();

    importPanel.checkSelectedFiles();
    importPanel.selectFiles(path3);
    importPanel.checkSelectedFiles(path3);

    importPanel.startImport();
    importPanel.checkFileContent(new Object[][]{
      {"02/01/2001", "Big Mac", "-2.00"}
    });

    importPanel.enterAccountNumber("0123546");
    importPanel.doImport();
    periods.selectAll();
    transactions.initContent()
      .add("02/01/2001", TransactionType.PRELEVEMENT, "Big Mac", "", -2.00)
      .check();
  }


  public void testImportQifBadFile() throws Exception {
    String path = org.globsframework.utils.TestUtils.getFileName(this, ".qif");
    Files.dumpStringToFile(path,
                           "Dsdfsdf sdfsf\n" +
                           "^sdfsf");
    ImportChecker importPanel = new ImportChecker(window);

    importPanel.selectBank("Societe Generale");
    importPanel.selectFiles(path);
    importPanel.startImport();
    importPanel.checkErrorMessage("import.file.error", new File(path).getAbsolutePath());
  }

  public void testBadFormatForOfx() throws Exception {
    String path = org.globsframework.utils.TestUtils.getFileName(this, ".ofx");
    Files.dumpStringToFile(path,
                           "<bad>\n" +
                           "sdfsdfsdf\n" +
                           "</bad>");
    ImportChecker importPanel = new ImportChecker(window);
    importPanel.selectFiles(path);
    importPanel.startImport();
    importPanel.checkErrorMessage("import.file.error", new File(path).getAbsolutePath());
  }


  private void checkImportMessage(String message) {
    TextBox accountMessage = window.getTextBox("importMessage");
    assertTrue(accountMessage.textEquals(message));
  }

  private void checkLoginMessage(String message) {
    TextBox fileMessage = window.findUIComponent(TextBox.class, message);
    assertTrue(fileMessage != null);
    assertTrue(fileMessage.isVisible());
  }

  private void checkErrorMessage(String message) {
    assertTrue(window.getTextBox("message").textContains(Lang.get(message)));
  }
}
