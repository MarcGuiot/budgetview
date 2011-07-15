package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.BankDateCellConverter;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.functests.checkers.converters.SeriesCellConverter;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.utils.IntSet;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.uispec4j.assertion.UISpecAssert.*;

public class TransactionChecker extends ViewChecker {
  public static final String TO_CATEGORIZE = "To categorize";

  private Table table;
  private Table amountTable;
  private CheckBox showPlannedTransactionsCheckbox;
  private ComboBox accountFilterCombo;
  private ComboBox seriesFilterCombo;

  public static TransactionChecker init(Window window) {
    return new TransactionChecker(window);
  }

  public TransactionChecker(Window window) {
    super(window);
  }

  public void checkTableIsEmpty() {
    assertTrue(getTable().isEmpty());
  }

  public ContentChecker initContent() {
    return new ContentChecker();
  }

  public Table getTable() {
    if (table == null) {
      views.selectData();
      table = mainWindow.getTable("transactionsTable");
      table.setCellValueConverter(TransactionView.DATE_COLUMN_INDEX, new DateCellConverter());
      table.setCellValueConverter(TransactionView.BANK_DATE_COLUMN_INDEX, new BankDateCellConverter());
      table.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter(true));
    }
    return table;
  }

  protected UIComponent getMainComponent() {
    return mainWindow.findUIComponent(ComponentMatchers.innerNameIdentity("transactionsTable"));
  }

  public TransactionChecker categorizePopup(int row) {
    PopupMenuInterceptor
      .run(getTable().triggerRightClick(row, 0))
      .getSubMenu("Categorize")
      .click();
    return this;
  }

  public TransactionChecker categorize(String... labels) {
    select(labels);
    clickSeries(getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, labels[0].toUpperCase()));
    return this;
  }

  public TransactionChecker selectAll() {
    getTable().selectAllRows();
    return this;
  }

  public TransactionChecker select(String... labels) {
    getTable().selectRowsWithText(TransactionView.LABEL_COLUMN_INDEX, Strings.toUpperCase(labels));
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

  public void checkSeriesTooltipContains(String transactionLabel, String text) {
    assertThat(getSeriesButton(getIndexOf(transactionLabel)).tooltipContains(text));
  }

  private void clickSeries(int rowIndex) {
    getSeriesButton(rowIndex).click();
  }

  private Button getSeriesButton(int rowIndex) {
    return getTable().editCell(rowIndex, TransactionView.SERIES_COLUMN_INDEX).getButton();
  }

  public TransactionChecker checkCategorizeIsDisabled(int row) {
    UISpecAssert.assertFalse(getSeriesButton(row).isEnabled());
    return this;
  }

  public void checkSeries(String transactionLabel, String seriesName) {
    checkSeries(getIndexOf(transactionLabel.toUpperCase()), seriesName);
  }

  public void checkSeries(int row, String seriesName) {
    Button seriesButton = getSeriesButton(row);
    assertThat(seriesButton.textEquals(seriesName));
  }

  private int getIndexOf(String transactionLabel) {
    assertThat(getTable().containsRow(TransactionView.LABEL_COLUMN_INDEX, transactionLabel));
    return getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, transactionLabel);
  }

  public void setSearchText(String text) {
    getSearchField().setText(text);
  }

  public void checkSearchTextIsEmpty() {
    assertThat(getSearchField().textIsEmpty());
  }

  public void clearSearch() {
    getSearchField().clear();
  }

  public TextBox getSearchField() {
    return mainWindow.getInputTextBox("searchField");
  }

  public TransactionChecker checkClearFilterButtonShown() {
    views.selectData();
    checkComponentVisible(this.mainWindow, JPanel.class, "customFilterMessage", true);
    return this;
  }

  public TransactionChecker checkClearFilterButtonHidden() {
    views.selectData();
    checkComponentVisible(this.mainWindow, JPanel.class, "customFilterMessage", false);
    return this;
  }

  public TransactionChecker clearFilters() {
    this.mainWindow.getPanel("customFilterMessage").getButton().click();
    return this;
  }

  public TransactionAmountChecker initAmountContent() {
    return new TransactionAmountChecker(getAmountTable());
  }

  private Table getAmountTable() {
    if (amountTable == null) {
      views.selectData();
      amountTable = mainWindow.getTable(Transaction.TYPE.getName());
      amountTable.setCellValueConverter(TransactionView.DATE_COLUMN_INDEX, new DateCellConverter());
      amountTable.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter(false));
    }
    return amountTable;
  }

  public void checkSelectedRow(int row) {
    assertTrue(table.rowIsSelected(row));
  }

  public void selectAccount(String accountName) {
    getAccountFilter().select(accountName);
  }

  private ComboBox getAccountFilter() {
    if (accountFilterCombo == null) {
      views.selectData();
      accountFilterCombo = mainWindow.getComboBox("accountFilterCombo");
    }
    return accountFilterCombo;
  }

  public void checkSelectedAccount(String selection) {
    assertThat(getAccountFilter().selectionEquals(selection));
  }

  public void selectSeries(String seriesName) {
    getSeriesFilter().select(seriesName);
  }

  public void checkSelectedSeries(String seriesName) {
    assertThat(getSeriesFilter().selectionEquals(seriesName));
  }

  private ComboBox getSeriesFilter() {
    if (seriesFilterCombo == null) {
      views.selectData();
      seriesFilterCombo = mainWindow.getComboBox("seriesFilterCombo");
    }
    return seriesFilterCombo;
  }

  public TransactionChecker checkSelectableSeries(String... series) {
    assertThat(getSeriesFilter().contentEquals(series));
    return this;
  }

  public void checkNotEmpty() {
    UISpecAssert.assertFalse(getTable().isEmpty());
  }

  public void delete(String label) {
    openDeletionDialog(getIndexOf(label.toUpperCase()))
      .checkTitle("Delete operations")
      .validate();
  }

  public void delete(String label, String message) {
    openDeletionDialog(getIndexOf(label.toUpperCase()))
      .checkTitle("Delete operations")
      .checkMessageContains(message)
      .validate();
  }

  public void delete(int row, int... additionalRows) {
    openDeletionDialog(org.globsframework.utils.Utils.join(row, additionalRows))
      .checkTitle("Delete operations")
      .validate();
  }

  public void deleteAll(String message) {
    table.selectAllRows();
    openDeletionDialog()
      .checkTitle("Delete operations")
      .checkMessageContains(message)
      .validate();
  }

  public void delete(int row, String message) {
    openDeletionDialog(row)
      .checkTitle("Delete operations")
      .checkMessageContains(message)
      .validate();
  }

  public void deleteTransactionWithNote(String note, String message) {
    int row = getTable().getRowIndex(TransactionView.NOTE_COLUMN_INDEX, note);
    Assert.assertTrue(note + " not found", row >= 0);
    openDeletionDialog(row)
      .checkTitle("Delete operations")
      .checkMessageContains(message)
      .validate();
  }

  public void deleteTransactionsWithNotes(String message, String... notes) {
    if (notes.length == 0) {
      Assert.fail("You must provide at least one note");
    }
    IntSet rowSet = new IntSet();
    for (String note : notes) {
      int[] rows = getTable().getRowIndices(TransactionView.NOTE_COLUMN_INDEX, note);
      Assert.assertTrue(note + " not found", rows.length > 0);
      rowSet.addAll(rows);
    }
    openDeletionDialog(rowSet.toIntArray())
      .checkMessageContains(message)
      .validate();
  }

  public void checkDeletionForbidden(int[] rows, String message) {
    openDeletionForbiddenDialog(rows)
      .checkTitle("Deletion denied")
      .checkMessageContains(message)
      .close();
  }

  public void checkDeletionForbidden(int row, String message) {
    openDeletionForbiddenDialog(row)
      .checkTitle("Deletion denied")
      .checkMessageContains(message)
      .close();
  }

  private ConfirmationDialogChecker openDeletionDialog(int row) {
    return new ConfirmationDialogChecker(getDeleteDialog(row));
  }

  private ConfirmationDialogChecker openDeletionDialog(int[] rows) {
    return new ConfirmationDialogChecker(getDeleteDialog(rows));
  }

  private ConfirmationDialogChecker openDeletionDialog() {
    return new ConfirmationDialogChecker(getDeleteDialog());
  }

  private MessageDialogChecker openDeletionForbiddenDialog(int row) {
    return new MessageDialogChecker(getDeleteDialog(row));
  }

  private MessageDialogChecker openDeletionForbiddenDialog(int[] rows) {
    return new MessageDialogChecker(getDeleteDialog(rows));
  }

  private Window getDeleteDialog(int row) {
    getTable().selectRow(row);
    return WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        KeyUtils.pressKey(getTable(), Key.DELETE);
      }
    });
  }

  private Window getDeleteDialog(int[] rows) {
    getTable().selectRows(rows);
    return WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        KeyUtils.pressKey(getTable(), Key.DELETE);
      }
    });
  }

  private Window getDeleteDialog() {
    return WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        KeyUtils.pressKey(getTable(), Key.DELETE);
      }
    });
  }

  public void editNote(int row, String note) {
    table.editCell(row, TransactionView.NOTE_COLUMN_INDEX, note, true);
  }

  public void editNote(String transactionLabel, String note) {
    editNote(getIndexOf(transactionLabel), note);
  }

  public TransactionChecker hidePlannedTransactions() {
    getShowPlannedTransactionsCheckbox().unselect();
    return this;
  }

  public void checkShowsPlannedTransaction(boolean show) {
    UISpecAssert.assertEquals(show, getShowPlannedTransactionsCheckbox().isSelected());
  }

  public TransactionChecker showPlannedTransactions() {
    getShowPlannedTransactionsCheckbox().select();
    return this;
  }

  private CheckBox getShowPlannedTransactionsCheckbox() {
    if (showPlannedTransactionsCheckbox == null) {
      views.selectData();
      showPlannedTransactionsCheckbox = mainWindow.getCheckBox("showPlannedTransactions");
    }
    return showPlannedTransactionsCheckbox;
  }

  public void copy(int row, int... rows) {
    getTable().selectRows(Utils.join(row, rows));
    PopupMenuInterceptor
      .run(getTable().triggerRightClick(row, 0))
      .getSubMenu("Copy")
      .click();
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
      assertThat(getTable().contentEquals(
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

    public void dumpCode() {
      Table table = getTable();
      StringBuilder builder = new StringBuilder();
      for (int row = 0; row < table.getRowCount(); row++) {
        String date = table.getContentAt(row, TransactionView.DATE_COLUMN_INDEX).toString();
        String series = table.getContentAt(row, TransactionView.SERIES_COLUMN_INDEX, new TableCellValueConverter() {
          public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
            return SeriesCellConverter.extractSeries(renderedComponent);
          }
        }).toString();
        String label = table.getContentAt(row, TransactionView.LABEL_COLUMN_INDEX).toString();
        String amount = table.getContentAt(row, TransactionView.AMOUNT_COLUMN_INDEX).toString();
        String position = table.getContentAt(row, TransactionView.ACCOUNT_BALANCE_INDEX).toString();
        String totalPosition = table.getContentAt(row, TransactionView.BALANCE_INDEX).toString();
        String accountName = table.getContentAt(row, TransactionView.ACCOUNT_NAME_INDEX).toString();

        builder.append(".add(\"")
          .append(date).append("\", \"");
        builder
          .append(label).append("\", ")
          .append(amount);

        boolean hasSeries = !TO_CATEGORIZE.equals(series) && Strings.isNotEmpty(series);
        if (!hasSeries) {
          series = "To categorize";
        }
        builder
          .append(", \"")
          .append(series)
          .append("\"");
        if (Strings.isNotEmpty(position)) {
          builder.append(", ")
            .append(position);
        }
        builder
          .append(", ")
          .append(totalPosition)
          .append(", \"")
          .append(accountName)
          .append("\")\n");
      }
      builder.append(".check();\n");
      Assert.fail("Use this code:\n" + builder.toString());
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
      return add(date, date, type, label, note, amount, series);
    }

    public ContentChecker add(String date, String bankDate, TransactionType type, String label,
                              String note, double amount) {
      return add(date, bankDate, type, label, note, amount, TO_CATEGORIZE);
    }

    public ContentChecker add(String date, TransactionType type, String label,
                              String note, double amount) {
      add(date, date, type, label, note, amount, TO_CATEGORIZE);
      return this;
    }

    public ContentChecker add(String userDate, String bankDate, TransactionType type, String label,
                              String note, double amount, String series) {
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
      add(new Object[]{userDate, bankDate, "(" + type.getName() + ")" + series, label,
                       TransactionChecker.this.toString(amount),
                       note});
      return this;
    }

    public void dumpCode() {
      TransactionTypeDumper transactionTypeDumper = new TransactionTypeDumper();

      final StringBuilder builder = new StringBuilder();
      builder.append(".initContent()\n");
      Table table = getTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        String type = table.getContentAt(row, 0, transactionTypeDumper).toString();
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
        if (hasSeries) {
          builder
            .append(", \"")
            .append(series)
            .append("\"");
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
        if (transaction.isTrue(Transaction.PLANNED)) {
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
                                                            Lang.get("label"),
                                                            Lang.get("amount"),
                                                            Lang.get("note")},
                                               expectedContent));
    }
  }
}
