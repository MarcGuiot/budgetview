package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.AmountCellConverter;
import org.designup.picsou.functests.checkers.converters.CategoryCellConverter;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.uispec4j.*;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowInterceptor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class TransactionChecker extends DataChecker {
  private Table table;
  private Window window;
  public static final String TO_CATEGORIZE = "To categorize";

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
      table.setCellValueConverter(TransactionView.CATEGORY_COLUMN_INDEX, new CategoryCellConverter(window));
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
    chooseCategoryViaButtonClick(DataChecker.getCategoryName(category), rows[0]);
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

  public SplitDialogChecker openSplitDialog(final int row) {
    return new SplitDialogChecker(WindowInterceptor.getModalDialog(new Trigger() {
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

  static String stringifyCategoryNames(MasterCategory... categories) {
    if ((categories.length == 0) || ((categories.length == 1) && categories[0].equals(MasterCategory.NONE))) {
      return TO_CATEGORIZE;
    }
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
    if (categories.length == 0) {
      return TO_CATEGORIZE;
    }
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
                              String note, double amount, MasterCategory category, MasterCategory... categories) {
      add(date, type, label, note, amount, stringifyCategories(category, categories));
      return this;
    }

    private String stringifyCategories(MasterCategory category, MasterCategory... categories) {
      if (MasterCategory.NONE.equals(category)) {
        return stringifyCategoryNames(categories);
      }
      return getCategoryName(category) +
             (categories.length == 0 ? "" : ", " + stringifyCategoryNames(categories));
    }

    public void dumpCode() {
      TransactionTypeDumper transactionDumper = new TransactionTypeDumper();

      StringBuilder builder = new StringBuilder();
      builder.append(".initContent()\n");
      Table table = getTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        String date = table.getContentAt(row, 0).toString();
        String type = table.getContentAt(row, 1, transactionDumper).toString();
        String label = table.getContentAt(row, 2).toString();
        String amount = table.getContentAt(row, 3).toString();
        String note = table.getContentAt(row, 4).toString();

        builder.append(".add(\"")
          .append(date).append("\", ")
          .append(type).append(", \"")
          .append(label).append("\", \"")
          .append(note).append("\", ")
          .append(amount).append(");\n");
      }
      builder.append(".check()");
      System.out.println(builder.toString());
    }

    private class TransactionTypeDumper implements TableCellValueConverter {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob)modelObject;
        TransactionType type = TransactionType.getType(transaction.get(Transaction.TRANSACTION_TYPE));
        if (type == null) {
          return "";
        }
        return "TransactionType." + type.getName().toUpperCase();
      }
    }

    public void check() {
      Object[][] expectedContent = content.toArray(new Object[content.size()][]);
      assertTrue(getTable().contentEquals(expectedContent));
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
      Mouse.doClickInRectangle(dialog.getTextBox("label." + categoryName),
                               new Rectangle(5, 5), false, Key.Modifier.NONE);
    }

    public CategoryChooserDialog checkContains(String[] expectedCategories) {
      for (int i = 0; i < expectedCategories.length; i++) {
        Assert.assertNotNull(window.getTextBox(expectedCategories[i]));
      }
      return this;
    }
  }
}
