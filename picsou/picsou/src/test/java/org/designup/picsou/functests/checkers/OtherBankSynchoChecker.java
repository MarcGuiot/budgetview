package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.assertion.UISpecAssert;

public class OtherBankSynchoChecker extends GuiChecker {
  private ImportDialogChecker importDialogChecker;
  private Window window;


  public OtherBankSynchoChecker(ImportDialogChecker importDialogChecker, Window window) {
    this.importDialogChecker = importDialogChecker;
    this.window = window;
  }

  public OtherBankSynchoChecker createNew(String type, String name, String position) {
    window.getButton("add").click();
    Table table = window.getTable("table");
    table.selectRow(table.getRowCount() - 1);
    window.getInputTextBox("type").setText(type);
    window.getInputTextBox("name").setText(name);
    window.getInputTextBox("position").setText(position);
    return this;
  }

  public OtherBankSynchoChecker select(int row){
    Table table = window.getTable("table");
    table.selectRow(row);
    return this;
  }

  public OtherBankSynchoChecker createNew(String type, String name, String position, String file) {
    createNew(type, name, position);
    return setFile(file);
  }

  public OtherBankSynchoChecker setFile(String file) {
    window.getInputTextBox("file").setText(file);
    return this;
  }

  public OtherBankSynchoChecker next() {
    window.getButton("update").click();
    return this;
  }

  public ImportDialogChecker doImport() {
    if (importDialogChecker != null) {
      window.getButton("update").click();
      UISpecAssert.assertFalse(window.isVisible());
      importDialogChecker.waitAcceptFiles();
    }
    else {
      importDialogChecker =
        ImportDialogChecker.openInStep2(window.getButton("update").triggerClick());
      UISpecAssert.assertFalse(window.isVisible());
    }
    return importDialogChecker;
  }

  public OtherBankSynchoChecker select(String name) {
    window.getCheckBox("import:" + name).select();
    return this;
  }
}
