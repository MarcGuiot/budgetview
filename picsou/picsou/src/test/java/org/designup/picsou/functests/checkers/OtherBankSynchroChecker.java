package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;
import org.uispec4j.Window;

public class OtherBankSynchroChecker extends SynchroChecker {

  public OtherBankSynchroChecker(ImportDialogChecker importDialogChecker, Window window) {
    super(importDialogChecker, window, "update");
  }

  public OtherBankSynchroChecker createAccount(String number, String name, String position) {
    window.getButton("add").click();
    Table table = window.getTable("table");
    table.selectRow(table.getRowCount() - 1);
    window.getInputTextBox("number").setText(number);
    window.getInputTextBox("name").setText(name);
    window.getInputTextBox("position").setText(position);
    return this;
  }

  public OtherBankSynchroChecker createAccount(String number, String name, String position, String file) {
    createAccount(number, name, position);
    if (file != null) {
      setFile(file);
    }
    return this;
  }

  public OtherBankSynchroChecker selectAccount(int row) {
    Table table = window.getTable("table");
    table.selectRow(row);
    return this;
  }

  public OtherBankSynchroChecker setFile(String file) {
    window.getInputTextBox("file").setText(file);
    return this;
  }

  public OtherBankSynchroChecker selectAccount(String name) {
    Table table = window.getTable();
    table.selectRow(table.getRowIndex(1, name));
    return this;
  }

  public OtherBankSynchroChecker setAmount(String position) {
    window.getInputTextBox("position").setText(position);
    return this;
  }

}
