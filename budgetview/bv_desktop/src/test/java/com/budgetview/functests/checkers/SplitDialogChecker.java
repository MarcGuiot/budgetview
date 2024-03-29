package com.budgetview.functests.checkers;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.transactions.split.SplitTransactionDialog;
import org.uispec4j.Button;
import org.uispec4j.Table;
import org.uispec4j.TableCellValueConverter;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class SplitDialogChecker extends GuiChecker {
  private Window window;
  private Table table;
  private Button closeButton;

  public SplitDialogChecker(Window window) {
    this.window = window;
    table = window.getTable();
    table.setCellValueConverter(SplitTransactionDialog.DELETE_SPLIT_COLUMN_INDEX, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        return "";
      }
    });
    closeButton = window.getButton("close");
  }

  public SplitDialogChecker enterAmount(String amount) {
    window.getInputTextBox("amount").setText(amount);
    return this;
  }

  public SplitDialogChecker enterNote(String note) {
    window.getInputTextBox("note").setText(note);
    return this;
  }

  public SplitDialogChecker assertOkDisabled() {
    assertFalse(getOkButton().isEnabled());
    return this;
  }

  public SplitDialogChecker checkErrorOnOk(String expectedMessage) {
    checkError(expectedMessage, "OK");
    return this;
  }

  public SplitDialogChecker checkErrorOnAdd(String expectedMessage) {
    checkError(expectedMessage, "Add");
    return this;
  }

  private void checkError(String expectedMessage, String buttonName) {
    window.getButton(buttonName).click();
    assertTrue(window.isVisible());
    checkTipVisible(window, window.getInputTextBox("amount"), expectedMessage);
  }

  public SplitDialogChecker checkNoError() {
    checkNoTipVisible(window);
    return this;
  }

  public SplitDialogChecker checkAmount(String displayedValue) {
    assertTrue(window.getInputTextBox("amount").textEquals(displayedValue));
    return this;
  }

  public SplitDialogChecker add() {
    getAddButton().click();
    return this;
  }

  public SplitDialogChecker add(double amount, String note) {
    enterAmount(Formatting.toString(amount));
    enterNote(note);
    add();
    return this;
  }

  public void validateAndClose() {
    getOkButton().click();
    assertFalse(window.isVisible());
  }

  public void close() {
    closeButton.click();
    assertFalse(window.isVisible());
  }

  public SplitDialogChecker checkTable(Object[][] objects) {
    Object[][] expected = new Object[objects.length][5];
    for (int i = 0; i < objects.length; i++) {
      int column = 0;
      expected[i] = new Object[]{
        (String) objects[i][column++],
        ((String) objects[i][column++]).toUpperCase(),
        Formatting.DECIMAL_FORMAT.format(objects[i][column++]),
        objects[i][column],
        ""};
    }
    assertTrue(table.contentEquals(expected));
    return this;
  }

  public SplitDialogChecker checkSelectedTableRow(int row) {
    UISpecAssert.assertThat(table.rowIsSelected(row));
    return this;
  }

  public SplitDialogChecker deleteRow(int row) {
    getDeleteButton(row).click();
    return this;
  }

  public SplitDialogChecker enterNoteInTable(int row, String note) {
    Table.Cell cell = table.editCell(row, SplitTransactionDialog.NOTE_COLUMN_INDEX);
    cell.getInputTextBox().setText(note);
    return this;
  }

  public SplitDialogChecker checkDeleteEnabled(int row, boolean enabled) {
    UISpecAssert.assertEquals(enabled, getDeleteButton(row).isEnabled());
    return this;
  }

  private Button getDeleteButton(int row) {
    Table.Cell cell = table.editCell(row, SplitTransactionDialog.DELETE_SPLIT_COLUMN_INDEX);
    return cell.getButton();
  }

  private Button getOkButton() {
    return window.getButton("OK");
  }

  private Button getAddButton() {
    return window.getButton("Add");
  }
}
