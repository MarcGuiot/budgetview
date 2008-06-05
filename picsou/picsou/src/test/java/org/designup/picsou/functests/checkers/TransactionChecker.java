package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.transactions.SplitTransactionDialog;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.utils.PicsouDescriptionService;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class TransactionChecker extends DataChecker {
  private Table table;
  private Window window;

  public TransactionChecker(Window window) {
    this.window = window;
  }

  public void checkHeader(String... columnNames) {
    assertTrue(getTable().getHeader().contentEquals(columnNames));
  }

  public void assertEmpty() {
    assertTrue(getTable().isEmpty());
  }

  public ContentChecker initContent() {
    return new ContentChecker();
  }

  public Table getTable() {
    if (table == null) {
      table = window.getTable(Transaction.TYPE.getName());
      table.setCellValueConverter(TransactionView.DATE_COLUMN_INDEX, new DateCellConverter());
      table.setCellValueConverter(TransactionView.CATEGORY_COLUMN_INDEX, new CategoryCellConverter());
      table.setCellValueConverter(TransactionView.AMOUNT_COLUMN_INDEX, new AmountCellConverter());
    }
    return table;
  }

  public static String getCategoryName(MasterCategory category) {
    if (category == MasterCategory.NONE) {
      return "";
    }
    return DataChecker.getCategoryName(category);
  }

  public void assignCategory(MasterCategory category, final int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaButtonClick(super.getCategoryName(category), rows[0]);
  }

  public void assignCategory(String subCategory, int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaButtonClick(subCategory, rows[0]);
  }

  public void assignCategoryViaKeyboard(MasterCategory category, int modifier, final int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaKeyboard(getCategoryName(category), modifier);
  }

  public void assignCategoryViaKeyboard(String subCategory, int modifier, int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaKeyboard(subCategory, modifier);
  }

  public SplitDialog openSplitDialog(final int row) {
    return new SplitDialog(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getTable().editCell(row, TransactionView.AMOUNT_COLUMN_INDEX).getButton().click();
      }
    }));
  }

  public CategoryChooserDialog openCategoryChooserDialog(final int... rows) {
    getTable().selectRows(rows);
    return new CategoryChooserDialog(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getTable().editCell(rows[0], TransactionView.CATEGORY_COLUMN_INDEX).getButton("Add").click();
      }
    }));
  }

  private void chooseCategoryViaButtonClick(String categoryName, final int row) {
    CategoryChooserDialog dialog = new CategoryChooserDialog(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getTable().editCell(row, TransactionView.CATEGORY_COLUMN_INDEX).getButton("Add").click();
      }
    }));
    dialog.selectCategory(categoryName);
  }

  private void chooseCategoryViaKeyboard(String categoryName, final int modifier) {
    CategoryChooserDialog dialog = new CategoryChooserDialog(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        GuiUtils.pressKey(getTable().getJTable(), KeyEvent.VK_SPACE, modifier);
      }
    }));
    dialog.selectCategory(categoryName);
  }

  private static String stringifyCategoryNames(MasterCategory... categories) {
    int index = 0;
    StringBuilder builder = new StringBuilder();
    for (MasterCategory category : categories) {
      if (index++ > 0) {
        builder.append(", ");
      }
      builder.append(getCategoryName(category));
    }
    return builder.toString();
  }

  private static String stringifySubCategoryNames(String... categories) {
    int index = 0;
    StringBuilder builder = new StringBuilder();
    for (String category : categories) {
      if (index++ > 0) {
        builder.append(", ");
      }
      builder.append(category);
    }
    return builder.toString();
  }

  public class ContentChecker {
    private List<Object[]> content = new ArrayList<Object[]>();

    private ContentChecker() {
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, String... category) {
      content.add(new Object[]{date, "(" + type.getName() + ")" + stringifySubCategoryNames(category), label,
                               TransactionChecker.this.toString(amount),
                               note});
      return this;
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, MasterCategory master, String category) {
      content.add(new Object[]{date, "(" + type.getName() + ")" + stringifyCategoryNames(master) + ", " + category, label,
                               TransactionChecker.this.toString(amount),
                               note});
      return this;
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, MasterCategory categorie, MasterCategory... categories) {
      add(date, type, label, note, amount, getCategoryName(categorie) + (categories.length != 0 ? ", " : "") +
                                           stringifyCategoryNames(categories));
      return this;
    }

    public void check() {
      Object[][] expectedContent = content.toArray(new Object[content.size()][]);
      assertTrue(getTable().contentEquals(expectedContent));
    }
  }

  public static class SplitDialog {
    private Window window;
    private Table splitsTable;
    private Button okButton;
    private Button cancelButton;
    private ToggleButton addAmountPanelToggle;
    private CheckBox dispensableBox;

    public SplitDialog(Window window) {
      this.window = window;
      splitsTable = window.getTable();
      splitsTable.setCellValueConverter(SplitTransactionDialog.CATEGORY_COLUMN_INDEX, new CategoryCellConverter());
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

    public SplitDialog assertAddAmountPanelVisible(boolean expected) {
      assertEquals(expected, getAddAmountPanel().isVisible());
      return this;
    }

    public SplitDialog toggleAddAmountPanel() {
      addAmountPanelToggle.click();
      return this;
    }

    public SplitDialog enterAmount(String amount) {
      window.getInputTextBox("amount").setText(amount);
      return this;
    }

    public SplitDialog enterNote(String note) {
      window.getInputTextBox("note").setText(note);
      return this;
    }

    public SplitDialog toggleDispensable() {
      dispensableBox.click();
      return this;
    }

    public SplitDialog checkDispensable(boolean expected) {
      assertEquals(expected, dispensableBox.isSelected());
      return this;
    }

    public SplitDialog checkNote(String text) {
      assertTrue(window.getInputTextBox("note").textEquals(text));
      return this;
    }

    public SplitDialog selectCategory(MasterCategory category) {
      CategoryChooserDialog dialog = new CategoryChooserDialog(WindowInterceptor.getModalDialog(new Trigger() {
        public void run() throws Exception {
          window.getButton("categoryChooser").click();
        }
      }));
      dialog.selectCategory(getCategoryName(category));
      return this;
    }

    public SplitDialog chooseCategory(final int row, MasterCategory category) {
      CategoryChooserDialog dialog = new CategoryChooserDialog(WindowInterceptor.run(new Trigger() {
        public void run() throws Exception {
          splitsTable.editCell(row, SplitTransactionDialog.CATEGORY_COLUMN_INDEX).getButton("Add").click();
        }
      }));
      dialog.selectCategory(getCategoryName(category));
      return this;
    }

    public SplitDialog assertAddDisabled() {
      assertFalse(getAddButton().isEnabled());
      return this;
    }

    public SplitDialog checkCurrentCategory(MasterCategory category) {
      assertTrue(window.getTextBox("category").textEquals(DataChecker.getCategoryName(category)));
      return this;
    }

    public SplitDialog checkErrorMessage(String expectedMessage) {
      Button addButton = getAddButton();
      addButton.click();
      assertTrue(window.getTextBox("message").textEquals(expectedMessage));
      assertAddAmountPanelVisible(true);
      return this;
    }

    public SplitDialog checkAmount(String displayedValue) {
      assertTrue(window.getInputTextBox("amount").textEquals(displayedValue));
      return this;
    }

    public SplitDialog add() {
      Button addButton = getAddButton();
      addButton.click();
      assertTrue(window.getTextBox("message").textIsEmpty());
      assertAddAmountPanelVisible(true);
      return this;
    }

    public SplitDialog cancelAddAmount() {
      window.getButton("cancelAdd").click();
      return this;
    }

    public void ok() {
      okButton.click();
      assertFalse(window.isVisible());
    }

    public void cancel() {
      cancelButton.click();
    }

    public SplitDialog checkTable(Object[][] objects) {
      Object[][] expected = new Object[objects.length][5];
      for (int i = 0; i < objects.length; i++) {
        int column = 0;
        expected[i][column] = "(" + ((TransactionType)objects[i][column]).getName() + ")" +
                              stringifyCategoryNames(((MasterCategory)objects[i][column + 1]));
        column++;
        expected[i][column] = objects[i][column + 1];
        column++;
        expected[i][column] = PicsouDescriptionService.DECIMAL_FORMAT.format(objects[i][column + 1]);
        column++;
        expected[i][column] = objects[i][column + 1];
        column++;
        expected[i][column] = "";
      }
      assertTrue(splitsTable.contentEquals(expected));
      return this;
    }

    public SplitDialog deleteRow(int row) {
      getDeleteButton(row).click();
      return this;
    }

    public SplitDialog add(String amount, MasterCategory category, String note) {
      enterAmount(amount);
      selectCategory(category);
      enterNote(note);
      add();
      return this;
    }

    public SplitDialog checkDeleteEnabled(boolean enabled, int row) {
      assertEquals(enabled, getDeleteButton(row).isEnabled());
      return this;
    }

    private Button getDeleteButton(int row) {
      Table.Cell cell = splitsTable.editCell(row, SplitTransactionDialog.REMOVE_SPLIT_COLUMN_INDEX);
      return cell.getButton();
    }

    private Button getAddButton() {
      return window.getButton("Ajouter");
    }

    private Panel getAddAmountPanel() {
      return window.getPanel("addAmountPanel");
    }
  }

  public static class CategoryChooserDialog {
    private Window window;

    public CategoryChooserDialog(Window window) {
      this.window = window;
    }

    public CategoryChooserDialog selectCategory(String categoryName) {
      selectCategory(window, categoryName);
      return this;
    }

    public static void selectCategory(Window dialog, String categoryName) {
      Mouse.doClickInRectangle(dialog.getTextBox("checkbox." + categoryName),
                               new Rectangle(5, 5), false, Key.Modifier.NONE);
    }

    public CategoryChooserDialog checkContains(String[] expectedCategories) {
      for (int i = 0; i < expectedCategories.length; i++) {
        Assert.assertNotNull(window.getTextBox(expectedCategories[i]));
      }
      return this;
    }
  }

  private static class DateCellConverter implements TableCellValueConverter {
    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      Glob transaction = (Glob)modelObject;
      int yearMonth = transaction.get(Transaction.MONTH);
      int year = Month.toYear(yearMonth);
      int month = Month.toMonth(yearMonth);
      int day = transaction.get(Transaction.DAY);
      return (day < 10 ? "0" : "") + day +
             "/" + (month < 10 ? "0" : "") + month +
             "/" + year;
    }
  }

  private static class CategoryCellConverter implements TableCellValueConverter {
    public Object getValue(int row, int column,
                           Component renderedComponent, Object modelObject) {
      org.uispec4j.Panel panel =
        new org.uispec4j.Panel((JPanel)renderedComponent);
      UIComponent[] categoryLabels = panel.getUIComponents(TextBox.class);
      int index = 0;
      StringBuilder builder = new StringBuilder();

      Integer transactionType = ((Glob)modelObject).get(Transaction.TRANSACTION_TYPE);
      builder.append("(");
      builder.append(TransactionType.getType(transactionType).getName());
      builder.append(")");
      for (int i = 0; i < categoryLabels.length; i++) {
        TextBox label = (TextBox)categoryLabels[i];
        if (index++ > 0) {
          builder.append(", ");
        }
        builder.append(label.getText());
      }
      return builder.toString().trim();
    }
  }

  private static class AmountCellConverter implements TableCellValueConverter {
    public Object getValue(int row, int column,
                           Component renderedComponent, Object modelObject) {
      org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
      return panel.getTextBox("amount").getText();
    }
  }
}
