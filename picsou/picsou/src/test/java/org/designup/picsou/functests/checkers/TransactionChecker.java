package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.functests.checkers.converters.SeriesCellConverter;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.SubSeries;
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
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.KeyUtils;

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
      table.setCellValueConverter(TransactionView.SUBSERIES_COLUMN_INDEX, new SubSeriesCellValueConverter(window));
      table.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter(true));
    }
    return table;
  }

  public void select(String... labels) {
    getTable().selectRowsWithText(TransactionView.LABEL_COLUMN_INDEX, labels);
  }

  protected UIComponent findMainComponent(Window window) {
    return window.findUIComponent(ComponentMatchers.innerNameIdentity(Transaction.TYPE.getName()));
  }

  public TransactionChecker categorize(String... labels) {
    getTable().selectRowsWithText(TransactionView.LABEL_COLUMN_INDEX, labels);
    clickSeries(getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, labels[0]));
    return this;
  }

  public TransactionChecker categorize(final int... rows) {
    Assert.assertTrue("You must specify at least one row index", rows.length > 0);
    if (rows.length > 1) {
      getTable().selectRows(rows);
    }
    clickSeries(rows[0]);
    return this;
  }

  private void clickSeries(int rowIndex) {
    getTable().editCell(rowIndex, TransactionView.SERIES_COLUMN_INDEX).getButton().click();
  }

  public TransactionChecker checkCategorizeIsDisabled(int row) {
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

  public void checkSeries(int row, String seriesName, String subSeries) {
    checkSeries(row, seriesName);
    UISpecAssert.assertThat(getTable().cellEquals(row, TransactionView.SUBSERIES_COLUMN_INDEX, subSeries));
  }

  private int getIndexOf(String transactionLabel) {
    return getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, transactionLabel.toUpperCase());
  }

  public void setSearchText(String text) {
    getSearchField().setText(text);
  }

  public void clearSearch() {
    getSearchField().clear();
  }

  public TextBox getSearchField() {
    return mainWindow.getInputTextBox("searchField");
  }

  public TransactionAmountChecker initAmountContent() {
    Table table = window.getTable(Transaction.TYPE.getName());
    table.setCellValueConverter(TransactionView.DATE_COLUMN_INDEX, new DateCellConverter());
    table.setCellValueConverter(TransactionView.SUBSERIES_COLUMN_INDEX, new SubSeriesCellValueConverter(window));
    table.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter(false));
    return new TransactionAmountChecker(table);
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

  public void checkNotEmpty() {
    UISpecAssert.assertFalse(getTable().isEmpty());
  }

  public ConfirmationDialogChecker delete(String label) {
    int row = getIndexOf(label.toUpperCase());
    if (row < 0) {
      row = getTable().getRowIndex(TransactionView.NOTE_COLUMN_INDEX, label);
    }
    Assert.assertTrue(label + " not found", row >= 0);
    return delete(row);
  }

  public ConfirmationDialogChecker delete(int row) {
    getTable().selectRow(row);
    Window deleteDialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        KeyUtils.pressKey(getTable(), Key.DELETE);
      }
    });
    return new ConfirmationDialogChecker(deleteDialog);
  }

  public void editNote(int row, String note) {
    table.editCell(row, TransactionView.NOTE_COLUMN_INDEX, note, true);
  }

  public void editNote(String transactionLabel, String note) {
    editNote(getIndexOf(transactionLabel), note);
  }

  public void showPlannedTransactions() {
    mainWindow.getCheckBox("showPlannedTransactions").select();
  }

  public void hidePlannedTransactions() {
    mainWindow.getCheckBox("showPlannedTransactions").unselect();
  }

  public void checkShowsPlannedTransaction(boolean show) {
    UISpecAssert.assertEquals(show, mainWindow.getCheckBox("showPlannedTransactions").isSelected());
  }

  public class TransactionAmountChecker {
    java.util.List<Object[]> expected = new ArrayList<Object[]>();
    private Table table;

    public TransactionAmountChecker(Table table) {
      this.table = table;
    }

    protected Table getTable() {
      return table;
    }

    public TransactionAmountChecker add(String date, String label, double amount, String seriesName,
                                        double totalBalance, String accountName) {
      return add(date, label, amount, seriesName, null, totalBalance, accountName);
    }

    public void check() {
      UISpecAssert.assertThat(getTable().contentEquals(
        new String[]{Lang.get("transactionView.date.user"),
                     Lang.get("label"),
                     Lang.get("amount"),
                     Lang.get("series"),
                     Lang.get("transactionView.account.position"),
                     Lang.get("transactionView.position"),
                     Lang.get("transactionView.account.name")
        },
        expected.toArray(new Object[expected.size()][])));
    }

    public void dump() {
      String[] columnNames = getTable().getHeader().getColumnNames();
      java.util.List list = Arrays.asList(columnNames);
      int dateIndex = list.indexOf(Lang.get("transactionView.date.user"));
      int label = list.indexOf(Lang.get("label"));
      int amount = list.indexOf(Lang.get("amount"));
      int accountBalance = list.indexOf(Lang.get("transactionView.account.position"));
      int balance = list.indexOf(Lang.get("transactionView.position"));
      int accountNameIndex = list.indexOf(Lang.get("transactionView.account.name"));
      int seriesIndex = list.indexOf(Lang.get("series"));
      int rowCount = getTable().getRowCount();
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < rowCount; i++) {
        buffer.append(".add(\"")
          .append(getTable().getContentAt(i, dateIndex)).append("\", \"")
          .append(getTable().getContentAt(i, label)).append("\", ")
          .append(getTable().getContentAt(i, amount)).append(", ");
        String series = table.getContentAt(i, seriesIndex, new TableCellValueConverter() {
          public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
            return SeriesCellConverter.extractSeries(renderedComponent);
          }
        }).toString();
        buffer.append("\"").append(series).append("\"");
        Object accountBalanceStr = getTable().getContentAt(i, accountBalance);
        if (!accountBalanceStr.equals("")) {
          buffer
            .append(", ")
            .append(accountBalanceStr);
        }
        buffer.append(", ")
          .append(getTable().getContentAt(i, balance));
        buffer.append(", \"")
          .append(getTable().getContentAt(i, accountNameIndex))
          .append("\"");

        buffer.append(")\n");
      }
      buffer.append(".check();\n");
      Assert.fail("Use this code:\n" + buffer.toString());
    }

    public TransactionAmountChecker add(String date, String label, double amount, String seriesName,
                                        Double accountBalance, double totalBalance, String accountName) {
      expected.add(new Object[]{date, label,
                                TransactionChecker.this.toString(amount),
                                seriesName,
                                TransactionChecker.this.toString(accountBalance),
                                TransactionChecker.this.toString(totalBalance),
                                accountName});

      return this;
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
                              String note, double amount, String series) {
      return add(date, type, label, note, amount, series, "");
    }

    public ContentChecker add(String userDate, String bankDate, TransactionType type, String label,
                              String note, double amount, String series, String subSeries) {
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
      add(new Object[]{userDate, bankDate, "(" + type.getName() + ")" + series, subSeries, label,
                       TransactionChecker.this.toString(amount),
                       note});
      return this;
    }

    public ContentChecker add(String date, String bankDate, TransactionType type, String label,
                              String note, double amount) {
      return add(date, bankDate, type, label, note, amount, TO_CATEGORIZE, "");
    }

    public ContentChecker add(String date, String bankDate, TransactionType type, String label,
                              String note, double amount, String series) {
      return add(date, bankDate, type, label, note, amount, series, "");
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount, String series, String subSeries) {
      return add(date, date, type, label, note, amount, series, subSeries);
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount) {
      add(date, type, label, note, amount, TO_CATEGORIZE);
      return this;
    }

    public void dumpCode() {
      TransactionTypeDumper transactionTypeDumper = new TransactionTypeDumper();

      final StringBuilder builder = new StringBuilder();
      builder.append(".initContent()\n");
      Table table = getTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        String type = table.getContentAt(row, 0, transactionTypeDumper).toString();
        String subSeries = table.getContentAt(row, TransactionView.SUBSERIES_COLUMN_INDEX).toString();
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
        boolean hasCategory = Strings.isNotEmpty(subSeries);
        if (hasSeries || hasCategory) {
          if (hasSeries) {
            builder
              .append(", \"")
              .append(series)
              .append("\"");
          }
          if (hasCategory) {
            builder.append(", ").append(subSeries);
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

    protected void add(Object[] row) {
      content.add(row);
    }

    public void check() {
      Object[][] expectedContent = content.toArray(new Object[content.size()][]);
      UISpecAssert.assertTrue(getTable()
        .contentEquals(new String[]{Lang.get("transactionView.date.user"),
                                    Lang.get("transactionView.date.bank"),
                                    Lang.get("series"),
                                    Lang.get("subSeries"),
                                    Lang.get("label"),
                                    Lang.get("amount"),
                                    Lang.get("note")},
                       expectedContent));
    }

  }

  private static class SubSeriesCellValueConverter implements TableCellValueConverter {
    private GlobRepository repository;

    private SubSeriesCellValueConverter(Window window) {
      Container container = window.getAwtComponent();
      if (container instanceof PicsouFrame) {
        PicsouFrame frame = (PicsouFrame)container;
        this.repository = frame.getRepository();
      }
      else if (container instanceof PicsouDialog) {
        PicsouFrame frame = (PicsouFrame)container.getParent();
        this.repository = frame.getRepository();
      }
    }

    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      Glob transaction = (Glob)modelObject;
      Glob subSeries = repository.findLinkTarget(transaction, Transaction.SUB_SERIES);
      if (subSeries == null) {
        return "";
      }
      return subSeries.get(SubSeries.NAME);
    }
  }
}
