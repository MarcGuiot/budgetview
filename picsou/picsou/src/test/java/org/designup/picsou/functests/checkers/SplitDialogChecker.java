package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.transactions.split.SplitTransactionDialog;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;

import java.awt.*;

public class SplitDialogChecker {
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

  public SplitDialogChecker checkNote(String text) {
    assertTrue(window.getInputTextBox("note").textEquals(text));
    return this;
  }

  public SplitDialogChecker assertOkDisabled() {
    assertFalse(getOkButton().isEnabled());
    return this;
  }

  public SplitDialogChecker checkOkFailure(String expectedMessage) {
    getOkButton().click();

    assertTrue(window.isVisible());

    TextBox message = window.getTextBox("message");
    assertTrue(message.isVisible());
    assertTrue(message.textEquals(expectedMessage));
    return this;
  }

  public SplitDialogChecker checkAmount(String displayedValue) {
    assertTrue(window.getInputTextBox("amount").textEquals(displayedValue));
    return this;
  }

  public SplitDialogChecker validate() {
    Button addButton = getOkButton();
    TextBox box = window.getTextBox("message");
    addButton.click();
    assertTrue(box.textIsEmpty());
    assertFalse(window.isVisible());
    return this;
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
        (String)objects[i][column++],
        ((String)objects[i][column++]).toUpperCase(),
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
}
