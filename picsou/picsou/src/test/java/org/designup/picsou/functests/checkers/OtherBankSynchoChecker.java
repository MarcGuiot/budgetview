package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;
import org.uispec4j.Window;

public class OtherBankSynchoChecker extends SynchroChecker {

  public OtherBankSynchoChecker(ImportDialogChecker importDialogChecker, Window window) {
    super(importDialogChecker, window, "update");
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

  public OtherBankSynchoChecker select(int row) {
    Table table = window.getTable("table");
    table.selectRow(row);
    return this;
  }

  public OtherBankSynchoChecker createNew(String type, String name, String position, String file) {
    createNew(type, name, position);
    if (file != null) {
      setFile(file);
    }
    return this;
  }

  public OtherBankSynchoChecker setFile(String file) {
    window.getInputTextBox("file").setText(file);
    return this;
  }

  public OtherBankSynchoChecker select(String name) {
    Table table = window.getTable();
    table.selectRow(table.getRowIndex(1, name));
    return this;
  }

  public void setAmount(String position) {
    window.getInputTextBox("position").setText(position);
  }
}
