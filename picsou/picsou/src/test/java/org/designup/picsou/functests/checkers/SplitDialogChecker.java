package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.transactions.split.SplitTransactionDialog;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import org.uispec4j.interception.WindowInterceptor;

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

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker selectEnvelope(MasterCategory category, boolean showSeriesInitialization) {
    CategorizationChecker dialog = new CategorizationChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getButton("category").click();
      }
    }));
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("groceries", category, showSeriesInitialization);
    return this;
  }

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker selectOccasional(MasterCategory category) {
    CategorizationChecker dialog = new CategorizationChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getButton("category").click();
      }
    }));
    dialog.selectOccasional();
    dialog.selectOccasionalSeries(category);
    return this;
  }

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker selectRecurring(String name, MasterCategory category, boolean showSeriesInitialization) {
    CategorizationChecker dialog = new CategorizationChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getButton("category").click();
      }
    }));
    dialog.selectRecurring();
    dialog.selectRecurringSeries(name, category, showSeriesInitialization);
    return this;
  }

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker chooseCategory(final int row, MasterCategory category) {
    CategoryChooserChecker dialog = new CategoryChooserChecker(WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        table.editCell(row, SplitTransactionDialog.CATEGORY_COLUMN_INDEX).getButton("Add").click();
      }
    }));
    dialog.selectCategory(TransactionChecker.getCategoryName(category), true);
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
    addButton.click();
    assertTrue(window.getTextBox("message").textIsEmpty());
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
      MasterCategory category = ((MasterCategory)objects[i][column++]);
      expected[i] = new Object[]{
        TransactionChecker.getCategoryName(category),
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

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker addEnvelope(String amount, MasterCategory category, String note) {
    enterAmount(amount);
    selectEnvelope(category, true);
    enterNote(note);
    validate();
    return this;
  }

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker addOccasional(String amount, MasterCategory category, String note) {
    enterAmount(amount);
    selectOccasional(category);
    enterNote(note);
    validate();
    return this;
  }

  /**
   * @deprecated A SUPPRIMER
   */
  public SplitDialogChecker addRecurring(String amount, String name, MasterCategory category, String note, boolean showSeriesInitialization) {
    enterAmount(amount);
    selectRecurring(name, category, showSeriesInitialization);
    enterNote(note);
    validate();
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
