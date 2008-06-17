package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class OperationChecker {
  private MenuItem importMenu;
  private MenuItem exportMenu;

  public OperationChecker(Window window) {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    importMenu = fileMenu.getSubMenu("Import");
    exportMenu = fileMenu.getSubMenu("Export");
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null, null);
  }

  public void importQifFile(Double balance, String file, String bank) {
    importFile(new String[]{file}, balance, bank);
  }

  public void importQifFiles(double balance, String bank, String... files) {
    importFile(files, balance, bank);
  }

  private void importFile(final String[] fileNames, final Double balance, final String bank) {
    WindowInterceptor
      .init(importMenu.triggerClick())
      .process(new WindowHandler() {

        public Trigger process(Window importDialog) throws Exception {

          WindowInterceptor.init(importDialog.getButton("Browse").triggerClick())
            .process(FileChooserHandler.init().select(fileNames))
            .run();

          if (bank != null) {
            importDialog.getComboBox("bank").select(bank);
          }
          importDialog.getButton("Import").click();
          if (importDialog.getInputTextBox("number").isEditable().isTrue()) {
            importDialog.getInputTextBox("number").setText("11111");
          }
          Button okButton = importDialog.getButton("OK");
          for (int i = 0; i < fileNames.length - 1; i++) {
            okButton.click();
          }

          return okButton.triggerClick();
        }
      })
      .run();
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
