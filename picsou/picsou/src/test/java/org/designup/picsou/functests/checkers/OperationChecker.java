package org.designup.picsou.functests.checkers;

import org.uispec4j.MenuItem;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.Button;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class OperationChecker {
  private MenuItem importMenu;
  private MenuItem exportMenu;

  public OperationChecker(Window window) {
    MenuItem fileMenu = window.getMenuBar().getMenu("Fichier");
    importMenu = fileMenu.getSubMenu("Importer");
    exportMenu = fileMenu.getSubMenu("Exporter");
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null);
  }

  public void importQifFile(Double balance, String ...name) {
    importFile(name, balance);
  }

  private void importFile(final String[] fileNames, final Double balance) {
    WindowInterceptor
      .init(importMenu.triggerClick())
      .process(new WindowHandler() {

        public Trigger process(Window importDialog) throws Exception {

          WindowInterceptor.init(importDialog.getButton("Parcourir").triggerClick())
            .process(FileChooserHandler.init().select(fileNames))
            .run();

          importDialog.getButton("Importer").click();

          Button okButton = importDialog.getButton("OK");
          for (int i = 0; i < fileNames.length - 1; i++) {
            okButton.click();
          }

          return okButton.triggerClick();
        }
      })
      .run();

    // TODO: QIF
//    if (name[0].endsWith(".qif")) {
//      interceptor.process("Solde", new WindowHandler() {
//        public Trigger process(Window window) throws Exception {
//          if (balance != null) {
//            TextBox textBox = window.getTextBox(TransactionImport.BALANCE.getName());
//            textBox.setText(Formats.DEFAULT_DECIMAL_FORMAT.format(balance));
//            return window.getButton("OK").triggerClick();
//          }
//          else {
//            return window.getButton("Inconnu").triggerClick();
//          }
//        }
//      });
//    }

  }

  public void exportFile(String name) {
    WindowInterceptor
      .init(exportMenu.triggerClick())
      .process(FileChooserHandler.init().select(name))
      .run();
  }

  public Trigger getImportTrigger() {
    return importMenu.triggerClick();
  }

  public Trigger getExportTrigger() {
    return exportMenu.triggerClick();
  }
}
