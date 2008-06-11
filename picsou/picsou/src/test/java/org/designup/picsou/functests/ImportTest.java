package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

public class ImportTest extends LoggedInFunctionalTestCase {
  protected Window window;
  protected TextBox fileField;
  protected Button importButton;
  protected ComboBox bankCombo;

  protected void setUp() throws Exception {
    super.setUp();
    window = WindowInterceptor.getModalDialog(operations.getImportTrigger());
    bankCombo = window.getComboBox("bankCombo");
    fileField = window.getInputTextBox("fileField");
    importButton = window.getButton("Import");
  }

  public void testStandardImport() throws Exception {

    bankCombo.select("CIC");
    assertNotNull(window.getTextBox("http://www.cic.fr/telechargements.cgi"));

    checkMessage("Select an OFX or QIF file to import");

    importButton.click();
    checkErrorMessage(window, "login.data.file.required");

    fileField.setText("blah.ofx");
    importButton.click();
    checkErrorMessage(window, "login.data.file.not.found");

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

    window.getInputTextBox("number").setText("0123546");

//    window.getInputTextBox("balance").setText("66.50");

    window.getButton("OK").click();

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testSkipFirstQifFile() throws Exception {
    final String path1 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "First operation")
      .save();
    final String path2 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/20", -2.2, "Second operation")
      .save();

    fileField.setText(path1 + ";" + path2);
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "First operation", "-1.10"}
    }));

    window.getButton("Skip").click();

    assertTrue(table.contentEquals(new Object[][]{
      {"20/01/2006", "Second operation", "-2.20"}
    }));

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

    window = WindowInterceptor.getModalDialog(operations.getImportTrigger());
    bankCombo = window.getComboBox("bankCombo");
    fileField = window.getInputTextBox("fileField");
    importButton = window.getButton("Import");
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

  private void checkMessage(String message) {
    TextBox fileMessage = (TextBox)window.findUIComponent(TextBox.class, message);
    assertTrue(fileMessage != null);
    assertTrue(fileMessage.isVisible());
  }

  private void checkErrorMessage(Window window, String message) {
    assertTrue(window.getTextBox("message").textContains(Lang.get(message)));
  }
}
