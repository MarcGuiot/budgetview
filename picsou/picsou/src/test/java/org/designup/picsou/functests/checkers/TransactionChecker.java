package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.functests.checkers.converters.SeriesCellConverter;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.description.CategoryStringifier;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class TransactionChecker extends ViewChecker {
  public static final String TO_CATEGORIZE = "To categorize";

  private Table table;
  private Window mainWindow;

  public TransactionChecker(Window window) {
    super(window);
    mainWindow = window;
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
      table.setCellValueConverter(TransactionView.CATEGORY_COLUMN_INDEX, new CategoryCellValueConverter(window));
      table.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter(window));
    }
    return table;
  }

  protected UIComponent findMainComponent(Window window) {
    return window.findUIComponent(ComponentMatchers.innerNameIdentity(Transaction.TYPE.getName()));
  }

  public static String getCategoryName(MasterCategory category) {
    if (category == MasterCategory.NONE) {
      return "";
    }
    return DataChecker.getCategoryName(category);
  }

  /**
   * @deprecated
   */
  public void assignOccasionalSeries(MasterCategory category, final int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaButtonClick(category, rows[0]);
  }

  /**
   * @deprecated
   */
  public void assignCategoryWithoutSelection(MasterCategory category, int row) {
    chooseCategoryViaButtonClick(category, row);
  }

  /**
   * @deprecated
   */
  public void assignCategory(MasterCategory master, String subCategory, int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaButtonClick(master, subCategory, rows[0]);
  }

  /**
   * @deprecated
   */
  public void assignCategoryViaKeyboard(MasterCategory category, int modifier, final int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaKeyboard(category, modifier);
  }

  /**
   * @deprecated en cours de suppression
   */
  public CategorizationChecker openCategorizationDialog(final int row) {
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getTable().editCell(row, TransactionView.SERIES_COLUMN_INDEX).getButton().click();
      }
    });
    return new CategorizationChecker(dialog);
  }

  /**
   * @deprecated en cours de suppression
   */
  private void chooseCategoryViaButtonClick(MasterCategory category, final int row) {
    CategorizationChecker checker = openCategorizationDialog(row);
    checker.selectOccasionalSeries(category);
  }

  /**
   * @deprecated en cours de suppression
   */
  private void chooseCategoryViaButtonClick(MasterCategory category, String subcat, final int row) {
    CategorizationChecker checker = openCategorizationDialog(row);
    checker.selectOccasionalSeries(category, subcat);
  }

  /**
   * @deprecated en cours de suppression
   */
  private void chooseCategoryViaKeyboard(MasterCategory category, final int modifier) {
    CategorizationChecker checker = new CategorizationChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        GuiUtils.pressKey(getTable().getJTable(), KeyEvent.VK_SPACE, modifier);
      }
    }));
    checker.selectOccasionalSeries(category);
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

  public void checkCategorizationDisabled(int clickedRow) {
    Button seriesButton = getTable().editCell(clickedRow, TransactionView.SERIES_COLUMN_INDEX).getButton();
    UISpecAssert.assertFalse(seriesButton.isEnabled());
  }

  public void checkSeries(String transactionLabel, String seriesName) {
    checkSeries(getIndexOf(transactionLabel), seriesName);
  }

  public void checkSeries(int row, String seriesName) {
    Button seriesButton = getTable().editCell(row, TransactionView.SERIES_COLUMN_INDEX).getButton();
    UISpecAssert.assertThat(seriesButton.textEquals(seriesName));
  }

  public void checkCategory(String label, MasterCategory category) {
    checkCategory(getIndexOf(label), category);
  }

  public void checkCategory(int row, MasterCategory category) {
    UISpecAssert.assertThat(getTable().cellEquals(row, TransactionView.CATEGORY_COLUMN_INDEX, getCategoryName(category)));
  }

  public void checkCategory(int row, String categoryName) {
    UISpecAssert.assertThat(getTable().cellEquals(row, TransactionView.CATEGORY_COLUMN_INDEX, categoryName));
  }

  private int getIndexOf(String transactionLabel) {
    return getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, transactionLabel);
  }

  public TextBox getSearchField() {
    return mainWindow.getInputTextBox("transactionSearchField");
  }

  public class ContentChecker extends TableChecker {

    private ContentChecker() {
    }

    protected Table getTable() {
      return TransactionChecker.this.getTable();
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, String series, MasterCategory category) {
      return add(date, type, label, note, amount, series, getCategoryName(category));
    }

    public ContentChecker addOccasional(String date, TransactionType type, String label,
                                        String note, double amount, MasterCategory category) {
      return add(date, type, label, note, amount, "Occasional", getCategoryName(category));
    }

    public ContentChecker addOccasional(String date, TransactionType type, String label,
                                        String note, double amount, String category) {
      return add(date, type, label, note, amount, "Occasional", category);
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, String... seriesAndCategory) {
      String category = "";
      String series = TO_CATEGORIZE;
      if (seriesAndCategory.length >= 1) {
        series = seriesAndCategory[0];
      }
      if (seriesAndCategory.length > 1) {
        category = seriesAndCategory[1];
      }
      add(new Object[]{date, date, "(" + type.getName() + ")" + series, category, label,
                       TransactionChecker.this.toString(amount),
                       note});
      return this;
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, MasterCategory category) {
      add(date, type, label, note, amount, stringifyCategory(category));
      return this;
    }

    private String stringifyCategory(MasterCategory category) {
      if (MasterCategory.NONE.equals(category)) {
        return TO_CATEGORIZE;
      }
      return getCategoryName(category);
    }

    public void dumpCode() {
      TransactionTypeDumper transactionDumper = new TransactionTypeDumper();
      CategoryDumper categoryDumper = new CategoryDumper();

      StringBuilder builder = new StringBuilder();
      builder.append(".initContent()\n");
      Table table = getTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        String date = table.getContentAt(row, 0).toString();
        String type = table.getContentAt(row, 1, transactionDumper).toString();
        String category = table.getContentAt(row, 1, categoryDumper).toString();
        String label = table.getContentAt(row, 2).toString();
        String amount = table.getContentAt(row, 3).toString();
        String note = table.getContentAt(row, 4).toString();

        builder.append(".add(\"")
          .append(date).append("\", ")
          .append(type).append(", \"")
          .append(label).append("\", \"")
          .append(note).append("\", ")
          .append(amount);
        if (Strings.isNotEmpty(category)) {
          builder.append(", ").append(category);
        }
        builder.append(")\n");
      }
      builder.append(".check();\n");
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

    private class CategoryDumper implements TableCellValueConverter {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob)modelObject;
        Integer categoryId = transaction.get(Transaction.CATEGORY);
        if (categoryId == null) {
          return "";
        }

        MasterCategory master = MasterCategory.findMaster(categoryId);
        if (master == MasterCategory.NONE) {
          return "";
        }
        if (master != null) {
          return "MasterCategory." + master.getName().toUpperCase();
        }

        org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
        UIComponent[] categoryLabels = panel.getUIComponents(TextBox.class);
        if (categoryLabels.length != 1) {
          return "???";
        }
        TextBox label = (TextBox)categoryLabels[0];
        return "\"" + label.getText() + "\"";
      }
    }
  }

  private static class CategoryCellValueConverter implements TableCellValueConverter {
    private GlobRepository repository;
    private CategoryStringifier categoryStringifier;

    private CategoryCellValueConverter(Window window) {
      Container container = window.getAwtComponent();
      if (container instanceof PicsouFrame) {
        PicsouFrame frame = (PicsouFrame)container;
        this.repository = frame.getRepository();
      }
      else if (container instanceof PicsouDialog) {
        PicsouFrame frame = (PicsouFrame)container.getParent();
        this.repository = frame.getRepository();
      }
      categoryStringifier = new CategoryStringifier();
    }

    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      Glob transaction = (Glob)modelObject;
      Glob category = repository.findLinkTarget(transaction, Transaction.CATEGORY);
      if (category == null || category.get(Category.ID).equals(Category.NONE)) {
        return "";
      }
      return categoryStringifier.toString(category, repository);
    }
  }
}
