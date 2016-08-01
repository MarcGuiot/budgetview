package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.HistoDailyChecker;
import com.budgetview.functests.checkers.components.PopupButton;
import com.budgetview.functests.checkers.converters.BankDateCellConverter;
import com.budgetview.functests.checkers.converters.DateCellConverter;
import com.budgetview.functests.checkers.converters.SeriesCellConverter;
import com.budgetview.gui.transactions.TransactionView;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionType;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.globsframework.model.Glob;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.IntSet;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
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

public class TransactionChecker extends FilteredViewChecker<TransactionChecker> {
  public static final String TO_CATEGORIZE = "To categorize";

  private Table table;
  private Table amountTable;
  private TextBox searchField;
  private boolean useDisplayedDates;
  private Panel transactionView;

  public static TransactionChecker init(Window window) {
    return new TransactionChecker(window);
  }

  public TransactionChecker(Window window) {
    super(window, "transactionView", "customFilterMessage");
  }

  public void checkEmpty() {
    assertTrue(getTable().isEmpty());
  }

  public ContentChecker initContent() {
    return new ContentChecker();
  }

  public Table getTable() {
    if (table == null) {
      views.selectData();
      table = getPanel().getTable("transactionsTable");
      table.setCellValueConverter(TransactionView.DATE_COLUMN_INDEX, new DateCellConverter(useDisplayedDates));
      table.setCellValueConverter(TransactionView.BANK_DATE_COLUMN_INDEX, new BankDateCellConverter(useDisplayedDates));
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

  public TransactionChecker checkCategorizePopupDisabled(int... rows) {
    if (rows.length == 0) {
      Assert.fail("At least one row must be selected");
    }
    getTable().selectRows(rows);
    assertFalse(PopupMenuInterceptor
                  .run(getTable().triggerRightClick(rows[0], 0))
                  .getSubMenu("Categorize")
                  .isEnabled());
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
    if (searchField == null) {
      searchField = getPanel().getInputTextBox("searchField");
    }
    return searchField;
  }


  public Panel getPanel() {
    if (transactionView == null) {
      transactionView = mainWindow.getPanel("transactionView");
    }
    return transactionView;
  }

  public TransactionAmountChecker initAmountContent() {
    return new TransactionAmountChecker(getAmountTable());
  }

  private Table getAmountTable() {
    if (amountTable == null) {
      views.selectData();
      amountTable = mainWindow.getPanel("transactionView").getTable(Transaction.TYPE.getName());
      amountTable.setCellValueConverter(TransactionView.DATE_COLUMN_INDEX, new DateCellConverter());
      amountTable.setCellValueConverter(TransactionView.SERIES_COLUMN_INDEX, new SeriesCellConverter(false));
    }
    return amountTable;
  }

  public void checkSelected(String label) {
    assertTrue(table.rowIsSelected(getIndexOf(label)));
  }

  public void checkSelectedRow(int row) {
    assertTrue(table.rowIsSelected(row));
  }

  public void checkNotEmpty() {
    UISpecAssert.assertFalse(getTable().isEmpty());
  }

  public void deleteAndUpdatePosition(String label) {
    openDeletionDialog(getIndexOf(label.toUpperCase()))
      .selectUpdatePosition()
      .checkTitle("Delete operations")
      .validate();
  }

  public void deleteWithoutUpdatingThePosition(String label) {
    openDeletionDialog(getIndexOf(label.toUpperCase()))
      .selectNoUpdateOfPosition()
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
      .checkErrorMessageContains(message)
      .close();
  }

  public void checkDeletionForbidden(int row, String message) {
    openDeletionForbiddenDialog(row)
      .checkTitle("Deletion denied")
      .checkErrorMessageContains(message)
      .close();
  }

  private TransactionDeleteChecker openDeletionDialog(int row) {
    return new TransactionDeleteChecker(getDeleteDialog(row));
  }

  private ConfirmationDialogChecker openDeletionDialog(int[] rows) {
    return new ConfirmationDialogChecker(getDeleteDialog(rows));
  }

  public TransactionDeleteChecker openDeletionDialog() {
    return new TransactionDeleteChecker(getDeleteDialog());
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

  public TransactionChecker showPlannedTransactions() {
    getShowPlannedCheckBox().select();
    return this;
  }

  public TransactionChecker hidePlannedTransactions() {
    getShowPlannedCheckBox().unselect();
    return this;
  }

  public TransactionChecker checkShowsPlannedTransactions() {
    assertThat(getShowPlannedCheckBox().isSelected());
    return this;
  }

  public TransactionChecker checkHidesPlannedTransactions() {
    assertFalse(getShowPlannedCheckBox().isSelected());
    return this;
  }

  private CheckBox getShowPlannedCheckBox() {
    return getPanel().getCheckBox(Lang.get("transactionView.showPlannedTransactions"));
  }

  private PopupButton openActionPopup() {
    views.selectData();
    return new PopupButton(getPanel().getButton("actionsMenu"));
  }

  public void copyTable() {
    openActionPopup().click(Lang.get("copyTable"));
  }

  public void copy(int row, int... rows) {
    getTable().selectRows(Utils.join(row, rows));
    PopupMenuInterceptor
      .run(getTable().triggerRightClick(row, 0))
      .getSubMenu("Copy")
      .click();
  }

  public TransactionChecker print() {
    openActionPopup().click(Lang.get("print.transactions.menu"));
    return this;
  }

  public TransactionChecker checkPrintDisabled() {
    openActionPopup().checkItemDisabled(Lang.get("print.transactions.menu"));
    return this;
  }

  public HistoDailyChecker checkGraph(String legend) {
    views.selectData();
    Panel panel = mainWindow.getPanel("transactionView");
    assertThat(panel.getTextBox("accountChartLegend").textEquals(legend));
    return new HistoDailyChecker(panel, "accountChart");
  }

  public TransactionChecker checkGraphShown() {
    checkComponentVisible(mainWindow, JPanel.class, "accountChart", true);
    return this;
  }

  public TransactionChecker checkGraphHidden() {
    checkComponentVisible(mainWindow, JPanel.class, "accountChart", false);
    return this;
  }

  public TransactionChecker showGraph() {
    openActionPopup().click(Lang.get("transactionView.showGraph"));
    return this;
  }

  public TransactionChecker hideGraph() {
    openActionPopup().click(Lang.get("transactionView.hideGraph"));
    return this;
  }

  public TransactionChecker sortByLabel() {
    return sortByColumn(TransactionView.LABEL_COLUMN_INDEX);
  }

  public TransactionChecker sortByEnvelope() {
    return sortByColumn(TransactionView.SERIES_COLUMN_INDEX);
  }

  public TransactionChecker sortByBankDate() {
    return sortByColumn(TransactionView.BANK_DATE_COLUMN_INDEX);
  }

  private TransactionChecker sortByColumn(int columnIndex) {
    getTable()
      .getHeader()
      .click(columnIndex);
    return this;
  }

  public void setUseDisplayedDates() {
    this.useDisplayedDates = true;
    this.table = null;
  }

  public TransactionEditionChecker edit(String label) {
    return edit(getIndexOf(label));
  }

  public TransactionEditionChecker edit(int rowIndex) {
    return edit(rowIndex, false);
  }

  public TransactionEditionChecker edit(int rowIndex, boolean plural) {
    return TransactionEditionChecker.open(PopupMenuInterceptor
                                            .run(getTable().triggerRightClick(rowIndex, 0))
                                            .getSubMenu(plural ? Lang.get("transaction.edition.action.multi") : Lang.get("transaction.edition.action.single"))
                                            .triggerClick());
  }

  public void checkEditionRejected(int[] rowIndices, String message) {
    Table table = getTable();
    table.selectRows(rowIndices);
    MessageDialogChecker.open(PopupMenuInterceptor
                                .run(table.triggerRightClick(rowIndices[0], 0))
                                .getSubMenu(rowIndices.length > 1 ? Lang.get("transaction.edition.action.multi") : Lang.get("transaction.edition.action.single"))
                                .triggerClick())
      .checkInfoMessageContains(message)
      .close();
  }

  public SeriesEditionDialogChecker editSeries(int... rowIndices) {
    if (rowIndices.length == 0) {
      Assert.fail("At least one index must be provided");
    }
    getTable().selectRows(rowIndices);
    return SeriesEditionDialogChecker.open(PopupMenuInterceptor
                                             .run(getTable().triggerRightClick(rowIndices[0], 0))
                                             .getSubMenu(Lang.get("transaction.editSeries"))
                                             .triggerClick());
  }

  public void editProject(int... rowIndices) {
    if (rowIndices.length == 0) {
      Assert.fail("At least one index must be provided");
    }
    getTable().selectRows(rowIndices);
    PopupMenuInterceptor
      .run(getTable().triggerRightClick(rowIndices[0], 0))
      .getSubMenu(Lang.get("transaction.editSeries"))
      .click();
  }

  public void checkEditSeriesDisabled(int... rowIndices) {
    if (rowIndices.length == 0) {
      Assert.fail("At least one index must be provided");
    }
    getTable().selectRows(rowIndices);
    assertThat(PopupMenuInterceptor
                 .run(getTable().triggerRightClick(rowIndices[0], 0))
                 .isEnabled());
  }

  public TransactionChecker checkAmountLabelColor(String label, String expectedColor) {
    assertThat(getTable().foregroundNear(getIndexOf(label),
                                         TransactionView.AMOUNT_COLUMN_INDEX,
                                         expectedColor));
    return this;
  }

  public void checkVisible(boolean visible) {
    checkComponentVisible(mainWindow, JPanel.class, "data", visible);
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
        Glob transaction = (Glob) modelObject;
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
