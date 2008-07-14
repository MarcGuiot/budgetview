package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.converters.CategoryCellConverter;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.transactions.split.SplitTransactionDialog;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.awt.*;

public class SplitDialogChecker {
  private Window window;
  private Table splitsTable;
  private Button okButton;
  private Button cancelButton;
  private ToggleButton addAmountPanelToggle;
  private CheckBox dispensableBox;

  public SplitDialogChecker(Window window) {
    this.window = window;
    splitsTable = window.getTable();
    splitsTable.setCellValueConverter(SplitTransactionDialog.CATEGORY_COLUMN_INDEX, new CategoryCellConverter(window));
    splitsTable.setCellValueConverter(SplitTransactionDialog.REMOVE_SPLIT_COLUMN_INDEX, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        return "";
      }
    });
    addAmountPanelToggle = window.getToggleButton("switchToAddAmountButton");
    dispensableBox = window.getCheckBox("dispensableBox");
    okButton = window.getButton("ok");
    cancelButton = window.getButton("cancel");
  }

  public SplitDialogChecker assertAddAmountPanelVisible(boolean expected) {
    org.uispec4j.assertion.UISpecAssert.assertEquals(expected, getAddAmountPanel().isVisible());
    return this;
  }

  public SplitDialogChecker toggleAddAmountPanel() {
    addAmountPanelToggle.click();
    return this;
  }

  public SplitDialogChecker enterAmount(String amount) {
    window.getInputTextBox("amount").setText(amount);
    return this;
  }

  public SplitDialogChecker enterNote(String note) {
    window.getInputTextBox("note").setText(note);
    return this;
  }

  public SplitDialogChecker toggleDispensable() {
    dispensableBox.click();
    return this;
  }

  public SplitDialogChecker checkDispensable(boolean expected) {
    org.uispec4j.assertion.UISpecAssert.assertEquals(expected, dispensableBox.isSelected());
    return this;
  }

  public SplitDialogChecker checkNote(String text) {
    org.uispec4j.assertion.UISpecAssert.assertTrue(window.getInputTextBox("note").textEquals(text));
    return this;
  }

  public SplitDialogChecker selectEnvelope(MasterCategory category, boolean showSeriesInitialization) {
    CategorizationDialogChecker dialog = new CategorizationDialogChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getButton("category").click();
      }
    }));
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("groceries", category, showSeriesInitialization);
    dialog.validate();
    return this;
  }

  public SplitDialogChecker selectOccasional(MasterCategory category) {
    CategorizationDialogChecker dialog = new CategorizationDialogChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getButton("category").click();
      }
    }));
    dialog.selectOccasional();
    dialog.selectOccasionalSeries(category);
    dialog.validate();
    return this;
  }

  public SplitDialogChecker selectRecurring(String name, boolean showSeriesInitialization) {
    CategorizationDialogChecker dialog = new CategorizationDialogChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getButton("category").click();
      }
    }));
    dialog.selectRecurring();
    dialog.selectRecurringSeries(name, showSeriesInitialization);
    dialog.validate();
    return this;
  }


  public SplitDialogChecker chooseCategory(final int row, MasterCategory category) {
    CategoryChooserChecker dialog = new CategoryChooserChecker(WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        splitsTable.editCell(row, SplitTransactionDialog.CATEGORY_COLUMN_INDEX).getButton("Add").click();
      }
    }));
    dialog.selectCategory(TransactionChecker.getCategoryName(category));
    return this;
  }

  public SplitDialogChecker assertAddDisabled() {
    org.uispec4j.assertion.UISpecAssert.assertFalse(getAddButton().isEnabled());
    return this;
  }

  public SplitDialogChecker checkCurrentCategory(MasterCategory category) {
//    org.uispec4j.assertion.UISpecAssert.assertTrue(window.getTextBox(ComponentMatchers.innerNameIdentity("category")).textEquals(DataChecker.getCategoryName(category)));
    return this;
  }

  public SplitDialogChecker checkErrorMessage(String expectedMessage) {
    Button addButton = getAddButton();
    addButton.click();
    org.uispec4j.assertion.UISpecAssert.assertTrue(window.getTextBox("message").textEquals(expectedMessage));
    assertAddAmountPanelVisible(true);
    return this;
  }

  public SplitDialogChecker checkAmount(String displayedValue) {
    org.uispec4j.assertion.UISpecAssert.assertTrue(window.getInputTextBox("amount").textEquals(displayedValue));
    return this;
  }

  public SplitDialogChecker add() {
    Button addButton = getAddButton();
    addButton.click();
    org.uispec4j.assertion.UISpecAssert.assertTrue(window.getTextBox("message").textIsEmpty());
    assertAddAmountPanelVisible(true);
    return this;
  }

  public SplitDialogChecker cancelAddAmount() {
    window.getButton("cancelAdd").click();
    return this;
  }

  public void ok() {
    okButton.click();
    org.uispec4j.assertion.UISpecAssert.assertFalse(window.isVisible());
  }

  public void cancel() {
    cancelButton.click();
  }

  public SplitDialogChecker checkTable(Object[][] objects) {
    Object[][] expected = new Object[objects.length][5];
    for (int i = 0; i < objects.length; i++) {
      int column = 0;
      expected[i][column] = "(" + ((TransactionType)objects[i][column]).getName() + ")" +
                            TransactionChecker.stringifyCategoryNames(((MasterCategory)objects[i][column + 1]));
      column++;
      expected[i][column] = objects[i][column + 1];
      column++;
      expected[i][column] = PicsouDescriptionService.DECIMAL_FORMAT.format(objects[i][column + 1]);
      column++;
      expected[i][column] = objects[i][column + 1];
      column++;
      expected[i][column] = "";
    }
    org.uispec4j.assertion.UISpecAssert.assertTrue(splitsTable.contentEquals(expected));
    return this;
  }

  public SplitDialogChecker deleteRow(int row) {
    getDeleteButton(row).click();
    return this;
  }

  public SplitDialogChecker addEnvelope(String amount, MasterCategory category, String note) {
    enterAmount(amount);
    selectEnvelope(category, true);
    enterNote(note);
    add();
    return this;
  }

  public SplitDialogChecker addOccasional(String amount, MasterCategory category, String note) {
    enterAmount(amount);
    selectOccasional(category);
    enterNote(note);
    add();
    return this;
  }

  public SplitDialogChecker addRecurring(String amount, String name, String note, boolean showSeriesInitialization) {
    enterAmount(amount);
    selectRecurring(name, showSeriesInitialization);
    enterNote(note);
    add();
    return this;
  }


  public SplitDialogChecker checkDeleteEnabled(boolean enabled, int row) {
    org.uispec4j.assertion.UISpecAssert.assertEquals(enabled, getDeleteButton(row).isEnabled());
    return this;
  }

  private Button getDeleteButton(int row) {
    Table.Cell cell = splitsTable.editCell(row, SplitTransactionDialog.REMOVE_SPLIT_COLUMN_INDEX);
    return cell.getButton();
  }

  private Button getAddButton() {
    return window.getButton("Add");
  }

  private Panel getAddAmountPanel() {
    return window.getPanel("addAmountPanel");
  }
}
