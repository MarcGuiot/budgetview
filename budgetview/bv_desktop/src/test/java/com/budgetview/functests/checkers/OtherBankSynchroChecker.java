package com.budgetview.functests.checkers;

import org.uispec4j.Table;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class OtherBankSynchroChecker extends SynchroChecker {

  public OtherBankSynchroChecker(ImportDialogChecker importDialogChecker, Window window) {
    super(importDialogChecker, window, "update");
  }

  public SynchroChecker checkPanelShown() {
//    assertThat(window.getTextBox("title").textEquals(Lang.get("import.synchro.title")));
    assertThat(window.getTable("table").isVisible());
    return this;
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

  public OtherBankSynchroChecker checkNoAccountDisplayed() {
    assertThat(window.getTable("table").isEmpty());
    return this;
  }

  public SynchroErrorDialogChecker checkIdentificationFailedError() {
    window.getComboBox().select("Identification failed");
    return SynchroErrorDialogChecker.init(window.getButton("update").triggerClick());
  }

  public SynchroErrorDialogChecker checkConnectionException() {
    window.getComboBox().select("Connection error");
    return SynchroErrorDialogChecker.init(window.getButton("update").triggerClick());
  }

  public OtherBankSynchroChecker clearErrors() {
    window.getComboBox().select("No error");
    return this;
  }

  public OtherBankSynchroChecker setCode(String code) {
    window.getInputTextBox("code").setText(code);
    return this;
  }

  public OtherBankSynchroChecker checkCode(String code) {
    assertThat(window.getInputTextBox("code").textEquals(code));
    return this;
  }

  public OtherBankSynchroChecker removeAccount(String account) {
    selectAccount(account);
    window.getButton("remove").click();
    return this;
  }
}
