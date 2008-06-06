package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

public class ImportTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {

    Window window = WindowInterceptor.getModalDialog(operations.getImportTrigger());

    ComboBox bankCombo = window.getComboBox("bankCombo");
    bankCombo.select("CIC");

    assertNotNull(window.getTextBox("http://www.cic.fr/telechargements.cgi"));

    TextBox fileField = window.getInputTextBox("fileField");
    Button importButton = window.getButton("Importer");

    TextBox fileMessage = (TextBox)window.findUIComponent(TextBox.class, "Indiquez l'emplacement");
    assertTrue(fileMessage != null);
    assertTrue(fileMessage.isVisible());

    importButton.click();
    checkErrorMessage(window, "login.data.file.required");

    fileField.setText("blah");
    importButton.click();
    checkErrorMessage(window, "login.data.file.not.found");

    final String path = OfxBuilder
      .init(ImportTest.this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    WindowInterceptor.init(window.getButton("Parcourir").triggerClick())
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

  private void checkErrorMessage(Window window, String message) {
    assertTrue(window.getTextBox("message").textContains(Lang.get(message)));
  }
}
