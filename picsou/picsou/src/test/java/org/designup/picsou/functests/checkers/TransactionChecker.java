package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
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
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TransactionChecker extends ViewChecker {
  public static final String TO_CATEGORIZE = "To categorize";

  private Table table;
  private Window mainWindow;

  public TransactionChecker(Window window) {
    super(window);
    mainWindow = window;
  }

  public void checkTableIsEmpty() {
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
      table.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter());
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
    return GuiChecker.getCategoryName(category);
  }

  public TransactionChecker categorize(final int... rows) {
    Assert.assertTrue("You must specify at least one row index", rows.length > 0);
    if (rows.length > 1) {
      getTable().selectRows(rows);
    }
    getTable().editCell(rows[0], TransactionView.SERIES_COLUMN_INDEX).getButton().click();
    return this;
  }

  public TransactionChecker checkCategorizeIsDisable(int row) {
    UISpecAssert.assertFalse(getTable().editCell(row, TransactionView.SERIES_COLUMN_INDEX).getButton().isEnabled());
    return this;
  }

  public void checkSeries(String transactionLabel, String seriesName) {
    checkSeries(getIndexOf(transactionLabel.toUpperCase()), seriesName);
  }

  public void checkSeries(int row, String seriesName) {
    Button seriesButton = getTable().editCell(row, TransactionView.SERIES_COLUMN_INDEX).getButton();
    UISpecAssert.assertThat(seriesButton.textEquals(seriesName));
  }

  public void checkCategory(String label, MasterCategory category) {
    checkCategory(getIndexOf(label.toUpperCase()), category);
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
    return mainWindow.getInputTextBox("searchField");
  }

  public TransactionAmountChecker initAmountContent() {
    return new TransactionAmountChecker();
  }

  public void checkSelectedRow(int row) {
    assertTrue(table.rowIsSelected(row));
  }

  public void selectAccount(String accountName) {
    mainWindow.getComboBox("accountFilterCombo").select(accountName);
  }

  public TransactionChecker checkSelectedAccount(String accountName) {
    UISpecAssert.assertThat(mainWindow.getComboBox("accountFilterCombo").selectionEquals(accountName));
    return this;
  }

  public class TransactionAmountChecker {
    java.util.List<Object[]> expected = new ArrayList<Object[]>();

    protected Table getTable() {
      return TransactionChecker.this.getTable();
    }

    public TransactionAmountChecker add(String label, double amount, double solde) {
      expected.add(new Object[]{label,
                                TransactionChecker.this.toString(amount),
                                "",
                                TransactionChecker.this.toString(solde)});
      return this;
    }

    public TransactionAmountChecker add(String label, double amount, double accountSolde, double solde) {
      expected.add(new Object[]{label,
                                TransactionChecker.this.toString(amount),
                                TransactionChecker.this.toString(accountSolde),
                                TransactionChecker.this.toString(solde)});
      return this;
    }

    public TransactionAmountChecker add(String label, double amount) {
      expected.add(new Object[]{label.toUpperCase(),
                                TransactionChecker.this.toString(amount),
                                "",
                                ""});
      return this;
    }

    public void check() {
      UISpecAssert.assertThat(getTable().contentEquals(
        new String[]{Lang.get("label"),
                     Lang.get("amount"),
                     Lang.get("transactionView.account.balance"),
                     Lang.get("transactionView.balance")},
        expected.toArray(new Object[expected.size()][])));
    }

    public void dump() {
      String[] columnNames = getTable().getHeader().getColumnNames();
      java.util.List list = Arrays.asList(columnNames);
      int label = list.indexOf(Lang.get("label"));
      int amount = list.indexOf(Lang.get("amount"));
      int accountBalance = list.indexOf(Lang.get("transactionView.account.balance"));
      int balance = list.indexOf(Lang.get("transactionView.balance"));
      int rowCount = getTable().getRowCount();
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < rowCount; i++) {
        buffer.append(".add(\"")
          .append(getTable().getContentAt(i, label)).append("\", ")
          .append(getTable().getContentAt(i, amount));
        if (!getTable().getContentAt(i, accountBalance).equals("") &&
            !getTable().getContentAt(i, balance).equals("")) {
          buffer
            .append(", ")
            .append(getTable().getContentAt(i, accountBalance)).append(", ")
            .append(getTable().getContentAt(i, balance));
        }
        if (getTable().getContentAt(i, accountBalance).equals("") &&
            !getTable().getContentAt(i, balance).equals("")) {
          buffer
            .append(", ")
            .append(getTable().getContentAt(i, balance));
        }

        buffer.append(")\n");
      }
      buffer.append(".check();\n");
      Assert.fail("Use this code:\n" + buffer.toString());
    }
  }

  public class ContentChecker {

    private java.util.List<Object[]> content = new ArrayList<Object[]>();

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

    public ContentChecker add(String userDate, String bankDate, TransactionType type, String label,
                              String note, double amount, String... seriesAndCategory) {
      String category = "";
      String series = TO_CATEGORIZE;
      if (seriesAndCategory.length >= 1) {
        series = seriesAndCategory[0];
      }
      if (seriesAndCategory.length > 1) {
        category = seriesAndCategory[1];
      }
      if (type == TransactionType.PLANNED) {
        if (amount > 0) {
          type = TransactionType.VIREMENT;
        }
        else {
          type = TransactionType.PRELEVEMENT;
        }
      }
      if (!label.startsWith("Planned")) {
        label = label.toUpperCase();
      }
      else {
        label = "Planned" + label.substring("Planned".length());
      }
      add(new Object[]{userDate, bankDate, "(" + type.getName() + ")" + series, category, label,
                       TransactionChecker.this.toString(amount),
                       note});
      return this;
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, String... seriesAndCategory) {
      return add(date, date, type, label, note, amount, seriesAndCategory);
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
      TransactionTypeDumper transactionTypeDumper = new TransactionTypeDumper();
      CategoryDumper categoryDumper = new CategoryDumper();

      final StringBuilder builder = new StringBuilder();
      builder.append(".initContent()\n");
      Table table = getTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        String type = table.getContentAt(row, 0, transactionTypeDumper).toString();
        String category = table.getContentAt(row, TransactionView.CATEGORY_COLUMN_INDEX, categoryDumper).toString();
        String date = table.getContentAt(row, TransactionView.DATE_COLUMN_INDEX).toString();
        String bankDate = table.getContentAt(row, TransactionView.BANK_DATE_COLUMN_INDEX).toString();
        String series = table.getContentAt(row, TransactionView.SERIES_COLUMN_INDEX, new TableCellValueConverter() {
          public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
            return SeriesCellConverter.extractSeries(renderedComponent);
          }
        }).toString();
        String label = table.getContentAt(row, TransactionView.LABEL_COLUMN_INDEX).toString();
        String amount = table.getContentAt(row, TransactionView.AMOUNT_COLUMN_INDEX).toString();
        String note = table.getContentAt(row, TransactionView.NOTE_COLUMN_INDEX).toString();

        builder.append(".add(\"")
          .append(date).append("\", ");
        if (!date.equals(bankDate)) {
          builder.append("\"").append(bankDate).append("\", ");
        }
        builder
          .append(type).append(", \"")
          .append(label).append("\", \"")
          .append(note).append("\", ")
          .append(amount);

        boolean hasSeries = !TO_CATEGORIZE.equals(series) && Strings.isNotEmpty(series);
        boolean hasCategory = Strings.isNotEmpty(category);
        if (hasSeries || hasCategory) {
          if (hasSeries) {
            builder
              .append(", \"")
              .append(series)
              .append("\"");
          }
          if (hasCategory) {
            builder.append(", ").append(category);
          }
        }
        builder.append(")\n");
      }
      builder.append(".check();\n");
      Assert.fail("Use this code:\n" + builder.toString());
    }

    private class TransactionTypeDumper implements TableCellValueConverter {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob)modelObject;
        TransactionType type = TransactionType.getType(transaction.get(Transaction.TRANSACTION_TYPE));
        if (type == null) {
          return "";
        }
        if (transaction.get(Transaction.PLANNED)) {
          type = TransactionType.PLANNED;
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

        TextBox label = null;
        if (renderedComponent instanceof JLabel) {
          label = new TextBox((JLabel)renderedComponent);
        }
        else if (renderedComponent instanceof JPanel) {
          org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
          UIComponent[] categoryLabels = panel.getUIComponents(TextBox.class);
          if (categoryLabels.length != 1) {
            return "???";
          }
          label = (TextBox)categoryLabels[0];
        }
        return "\"" + label.getText() + "\"";
      }
    }

    protected void add(Object[] row) {
      content.add(row);
    }

    public void check() {
      Object[][] expectedContent = content.toArray(new Object[content.size()][]);
      UISpecAssert.assertTrue(getTable()
        .contentEquals(new String[]{Lang.get("transactionView.date.user"),
                                    Lang.get("transactionView.date.bank"),
                                    Lang.get("series"),
                                    Lang.get("category"),
                                    Lang.get("label"),
                                    Lang.get("amount"),
                                    Lang.get("note")},
                       expectedContent));
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
