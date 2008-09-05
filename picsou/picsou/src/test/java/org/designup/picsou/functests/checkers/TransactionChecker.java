package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.converters.CategoryCellConverter;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
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
import java.util.ArrayList;
import java.util.List;

public class TransactionChecker extends ViewChecker {
  public static final String TO_CATEGORIZE = "To categorize";

  private Table table;
  private TransactionDetailsChecker transactionDetails;

  public TransactionChecker(Window window) {
    super(window);
    this.transactionDetails = new TransactionDetailsChecker(window);
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

  public void assignOccasionalSeries(MasterCategory category, final int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaButtonClick(category, rows[0]);
  }

  public void assignCategoryWithoutSelection(MasterCategory category, int row) {
    chooseCategoryViaButtonClick(category, row);
  }

  public void assignCategory(MasterCategory master, String subCategory, int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaButtonClick(master, subCategory, rows[0]);
  }

  public void assignCategoryViaKeyboard(MasterCategory category, int modifier, final int... rows) {
    getTable().selectRows(rows);
    chooseCategoryViaKeyboard(category, modifier);
  }

  public CategorizationDialogChecker openCategorizationDialog(final int row) {
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getTable().editCell(row, TransactionView.CATEGORY_COLUMN_INDEX).getButton().click();
      }
    });
    return new CategorizationDialogChecker(dialog);
  }

  private void chooseCategoryViaButtonClick(MasterCategory category, final int row) {
    CategorizationDialogChecker checker = openCategorizationDialog(row);
    checker.selectOccasionalSeries(category);
    checker.validate();
  }

  private void chooseCategoryViaButtonClick(MasterCategory category, String subcat, final int row) {
    CategorizationDialogChecker checker = openCategorizationDialog(row);
    checker.selectOccasionalSeries(category, subcat);
    checker.validate();
  }

  private void chooseCategoryViaKeyboard(MasterCategory category, final int modifier) {
    CategorizationDialogChecker checker = new CategorizationDialogChecker(WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        GuiUtils.pressKey(getTable().getJTable(), KeyEvent.VK_SPACE, modifier);
      }
    }));
    checker.selectOccasionalSeries(category);
    checker.validate();
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

  public CategorizationDialogChecker categorize(int... rows) {
    getTable().selectRows(rows);
    transactionDetails.checkCategorizationAvailable();
    return transactionDetails.categorize();
  }

  public void setExceptionalIncome(String label, String seriesName, boolean showSeriesInitialization) {
    int rowIndex = getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectIncome();
    categorization.selectExceptionalIncomeSeries(seriesName, showSeriesInitialization);
    categorization.validate();
  }

  public void setIncome(String label, String seriesName, boolean showSeriesInitialization) {
    int rowIndex = getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectIncome();
    categorization.selectIncomeSeries(seriesName, showSeriesInitialization);
    categorization.validate();
  }

  public void setRecurring(int rowIndex, String seriesName, MasterCategory category, boolean showSeriesInitialization) {
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectRecurring();
    categorization.selectRecurringSeries(seriesName, category, showSeriesInitialization);
    categorization.validate();
  }

  public void setRecurring(String label, String seriesName, MasterCategory category, boolean showSeriesInitialization) {
    int rowIndex = getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    setRecurring(rowIndex, seriesName, category, showSeriesInitialization);
  }

  public void setEnvelope(int rowIndex, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries(seriesName, master, showSeriesInitialization);
    categorization.validate();
  }

  public void setEnvelope(String label, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    int rowIndex = getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    setEnvelope(rowIndex, seriesName, master, showSeriesInitialization);
  }

  public void setOccasional(int rowIndex, MasterCategory category) {
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectOccasional();
    categorization.selectOccasionalSeries(category);
    categorization.validate();
  }

  public void setProject(String label, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    int rowIndex = getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    setProject(rowIndex, seriesName, master, showSeriesInitialization);
  }

  public void setProject(int rowIndex, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectProjects();
    categorization.selectProjectSeries(seriesName, master, showSeriesInitialization);
    categorization.validate();
  }

  public void setSavings(String label, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    int rowIndex = getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    setSavings(rowIndex, seriesName, master, showSeriesInitialization);
  }

  public void setSavings(int rowIndex, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    CategorizationDialogChecker categorization = categorize(rowIndex);
    categorization.selectSavings();
    categorization.selectSavingsSeries(seriesName, master, showSeriesInitialization);
    categorization.validate();
  }

  public void checkCategorizationDisabled(int clickedRow) {
    Button seriesButton = getTable().editCell(clickedRow, TransactionView.CATEGORY_COLUMN_INDEX).getButton();
    UISpecAssert.assertFalse(seriesButton.isEnabled());
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

    public void check() {
      Object[][] expectedContent = content.toArray(new Object[content.size()][]);
      assertTrue(getTable().contentEquals(expectedContent));
    }
  }

}
