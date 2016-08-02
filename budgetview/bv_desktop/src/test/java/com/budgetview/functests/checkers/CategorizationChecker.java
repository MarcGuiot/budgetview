package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.converters.DateCellConverter;
import com.budgetview.functests.checkers.converters.ReconciliationAnnotationCellConverter;
import com.budgetview.desktop.categorization.components.CategorizationFilteringMode;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.model.BudgetArea;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.model.Glob;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.Utils;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.KeyUtils;

import javax.swing.AbstractButton;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.uispec4j.assertion.UISpecAssert.*;

public class CategorizationChecker extends FilteredViewChecker<CategorizationChecker> {
  public static final int LABEL_COLUMN_INDEX = 2;
  public static final int AMOUNT_COLUMN_INDEX = 3;
  private Panel panel;
  private Panel selectionPanel;
  private boolean useDisplayedDates;

  public CategorizationChecker(Window mainWindow) {
    super(mainWindow, "categorizationSelector", "customFilterMessage");
  }

  Panel getSelectionPanel() {
    if (selectionPanel == null) {
      views.selectCategorization();
      selectionPanel = mainWindow.getPanel("categorizationSelector");
    }
    return selectionPanel;
  }

  Panel getPanel() {
    if (panel == null) {
      views.selectCategorization();
      panel = mainWindow.getPanel("categorizationView");
    }
    return panel;
  }

  Panel selectAndGetBudgetArea(BudgetArea budgetArea) {
    selectBudgetArea(budgetArea);

    Panel panel = getPanel(budgetArea);
    assertTrue(panel.isVisible());
    return panel;
  }

  private Panel getPanel(BudgetArea budgetArea) {
    return getPanel().getPanel(budgetArea.name().toLowerCase() + "SeriesChooser");
  }

  private void selectBudgetArea(BudgetArea budgetArea) {
    AbstractButton button = getBudgetAreasPanel().findSwingComponent(AbstractButton.class, budgetArea.getName());
    if (button == null) {
      Assert.fail("No button found for budget area " + budgetArea);
    }
    button.doClick(0);
  }

  private Panel getBudgetAreasPanel() {
    return getPanel().getPanel("budgetAreaSelectionPanel");
  }

  public CategorizationChecker checkLabel(String expected) {
    assertTrue(getTransactionLabel().textEquals(expected));
    return this;
  }

  public CategorizationChecker checkBudgetAreaSelectionPanelDisplayed() {
    for (BudgetArea area : BudgetArea.values()) {
      if ((area != BudgetArea.UNCATEGORIZED) && (area != BudgetArea.ALL)) {
        assertTrue(getPanel().containsSwingComponent(AbstractButton.class, area.getName()));
      }
    }
    return this;
  }

  public void checkNoSelectionPanelDisplayed() {
    Panel panel = getPanel();
    for (BudgetArea area : BudgetArea.values()) {
      if ((area != BudgetArea.UNCATEGORIZED) && (area != BudgetArea.ALL)) {
        assertFalse(area.getName(), panel.containsUIComponent(Button.class, area.getName()));
        assertFalse(area.getName(), panel.containsUIComponent(ToggleButton.class, area.getName()));
      }
    }
    checkComponentVisible(getPanel(), JPanel.class, "noSelectionPanel", true);
    assertThat(getPanel().getTextBox("noSelectionMessage").textContains("No operation selected"));

  }

  public void checkNoDataImportedMessage() {
    assertThat(getPanel().getPanel("noDataImportedPanel").getTextBox().textContains("No data imported"));
  }

  public void checkNoDataShownMessage() {
    assertThat(getPanel().getPanel("noDataShownPanel").getTextBox("noDataShownMessage").textContains("No data shown"));
  }

  public void checkBudgetAreaIsSelected(BudgetArea budgetArea) {
    assertTrue(getPanel().getToggleButton(budgetArea.getGlob().get(BudgetArea.NAME)).isSelected());
  }

  public BudgetAreaCategorizationChecker selectIncome() {
    return selectAndReturn(BudgetArea.INCOME);
  }

  public BudgetAreaCategorizationChecker selectRecurring() {
    return selectAndReturn(BudgetArea.RECURRING);
  }

  public BudgetAreaCategorizationChecker selectVariable() {
    return selectAndReturn(BudgetArea.VARIABLE);
  }

  public ExtrasBudgetAreaCategorizationChecker selectExtras() {
    return new ExtrasBudgetAreaCategorizationChecker(this);
  }

  public TransferCategorizationChecker selectTransfers() {
    return new TransferCategorizationChecker(this, BudgetArea.TRANSFER);
  }

  public OtherCategorizationChecker selectOther() {
    selectBudgetArea(BudgetArea.OTHER);
    return new OtherCategorizationChecker(getPanel(BudgetArea.OTHER), this);
  }

  public void copyBankFormatToClipboard() {
    JTable jTable = getTable().getAwtComponent();
    KeyUtils.pressKey(jTable, org.uispec4j.Key.plaformSpecificCtrl(org.uispec4j.Key.B));
  }

  public void checkEditSeriesNotVisible() {
    checkComponentVisible(getPanel(), JPanel.class, "groupCreateEditSeries", false);
  }

  public void checkUserDate(TransactionDetailsChecker details, String yyyyMMdd, String label) {
    selectTransaction(label);
    details.checkBudgetDate(yyyyMMdd);
  }

  public CategorizationChecker checkMultipleSeriesSelection() {
    assertThat(getPanel().getPanel("seriesCard").getInputTextBox().textContains("Multiple Series selection"));
    return this;
  }

  public CategorizationChecker checkTransfersPreSelected() {
    assertThat(getPanel().getToggleButton(BudgetArea.TRANSFER.getName()).isSelected());
    return this;
  }

  public CategorizationChecker checkRecurringPreSelected() {
    assertThat(getPanel().getToggleButton(BudgetArea.RECURRING.getName()).isSelected());
    return this;
  }

  public CategorizationChecker checkVariablePreSelected() {
    assertThat(getPanel().getToggleButton(BudgetArea.VARIABLE.getName()).isSelected());
    return this;
  }

  public CategorizationChecker checkYellowBgLabel(int row, int column) {
    assertThat(getTable().backgroundNear(row, column, Colors.toColor("3C6CC6")));
    return this;
  }

  public CategorizationChecker checkNormalBgColor(int row, int column) {
    assertThat(getTable().backgroundNear(row, column, Color.white));
    return this;
  }

  public CategorizationChecker setDeferred(String operationName, String seriesName) {
    selectTransaction(operationName)
      .selectOther()
      .selectDeferred()
      .selectSeries(seriesName);
    return this;
  }

  public void copy(int row, int... rows) {
    getTable().selectRows(Utils.join(row, rows));
    PopupMenuInterceptor
      .run(getTable().triggerRightClick(row, 0))
      .getSubMenu("Copy")
      .click();
  }

  public void search(String searchString) {
    getSelectionPanel().getTextBox("searchField").setText(searchString);
  }

  public void checkSearchCleared() {
    assertThat(getSelectionPanel().getTextBox("searchField").textIsEmpty());
  }

  public TransactionEditionChecker edit(int rowIndex) {
    return TransactionEditionChecker.open(PopupMenuInterceptor
                                            .run(getTable().triggerRightClick(rowIndex, 0))
                                            .getSubMenu("Edit")
                                            .triggerClick());
  }

  public CategorizationChecker checkAmountLabelColor(String label, String expectedColor) {
    assertThat(getTable().foregroundNear(getRowIndex(label), AMOUNT_COLUMN_INDEX, expectedColor));
    return this;
  }

  public void reconcile(String from, String to) {
    selectTransaction(from)
      .switchToReconciliation()
      .select(to).reconcile();
  }

  public class TransferCategorizationChecker extends BudgetAreaCategorizationChecker {
    private TransferCategorizationChecker(CategorizationChecker categorizationChecker, BudgetArea budgetArea) {
      super(categorizationChecker, budgetArea);
    }

    public TransferCategorizationChecker selectAndCreateTransferSeries(String savingsName, String fromAccount) {
      selectTransfers().createSeries()
        .setName(savingsName)
        .setFromAccount(fromAccount)
        .setToAccount("External account")
        .validate();
      return this;
    }

    public TransferCategorizationChecker selectAndCreateTransferSeries(String savingsName, String fromAccount, String toAccount) {
      selectTransfers().createSeries()
        .setName(savingsName)
        .setFromAccount(fromAccount)
        .setToAccount(toAccount)
        .validate();
      return this;
    }

    public TransferCategorizationChecker selectAndCreateTransferSeries(String savingsName, String fromAccount, String toAccount, double plannedAmount) {
      selectTransfers().createSeries()
        .setName(savingsName)
        .setFromAccount(fromAccount)
        .setToAccount(toAccount)
        .setAmount(plannedAmount)
        .validate();
      return this;
    }

    public AccountEditionChecker createSavingsAccount() {
      selectTransfers();
      return AccountEditionChecker.open(getPanel().getButton("New account").triggerClick());
    }
  }

  private BudgetAreaCategorizationChecker selectAndReturn(BudgetArea budgetArea) {
    return new BudgetAreaCategorizationChecker(this, budgetArea);
  }

  public BudgetAreaCategorizationChecker getIncome() {
    return checkSelectedAndReturn(BudgetArea.INCOME);
  }

  public BudgetAreaCategorizationChecker getRecurring() {
    return checkSelectedAndReturn(BudgetArea.RECURRING);
  }

  public BudgetAreaCategorizationChecker getVariable() {
    return checkSelectedAndReturn(BudgetArea.VARIABLE);
  }

  public ExtrasBudgetAreaCategorizationChecker getExtras() {
    return (ExtrasBudgetAreaCategorizationChecker) checkSelectedAndReturn(BudgetArea.EXTRAS);
  }

  public BudgetAreaCategorizationChecker getSavings() {
    return checkSelectedAndReturn(BudgetArea.TRANSFER);
  }

  public CategorizationChecker checkToCategorize() {
    assertThat(getPanel().getToggleButton(BudgetArea.UNCATEGORIZED.getName()).isSelected());
    return this;
  }

  private BudgetAreaCategorizationChecker checkSelectedAndReturn(BudgetArea budgetArea) {
    assertTrue(getPanel().getToggleButton(budgetArea.getName()).isSelected());
    if (BudgetArea.EXTRAS.equals(budgetArea)) {
      return new ExtrasBudgetAreaCategorizationChecker(this);
    }
    return new BudgetAreaCategorizationChecker(this, budgetArea);
  }

  public CategorizationChecker checkRecurringSeriesIsSelected(String seriesName) {
    return checkSeriesIsSelected(BudgetArea.RECURRING, seriesName);
  }

  public CategorizationChecker checkSavingsSeriesIsSelected(String seriesName) {
    return checkSeriesIsSelected(BudgetArea.TRANSFER, seriesName);
  }

  public CategorizationChecker checkIncomeSeriesIsSelected(String seriesName) {
    return checkSeriesIsSelected(BudgetArea.INCOME, seriesName);
  }

  public CategorizationChecker checkOtherSeriesIsSelected(String seriesName) {
    return checkSeriesIsSelected(BudgetArea.OTHER, seriesName);
  }

  private CategorizationChecker checkSeriesIsSelected(BudgetArea budgetArea, String seriesName) {
    String prefix = budgetArea.getName();
    assertTrue(getPanel().getToggleButton(prefix).isSelected());
    Panel panel = getPanel();
    assertTrue(panel.containsUIComponent(Panel.class, prefix + "SeriesChooser"));

    Panel recurringSeriesPanel = panel.getPanel(prefix + "SeriesChooser");
    assertTrue(recurringSeriesPanel.isVisible());
    assertTrue(recurringSeriesPanel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public SeriesEditionDialogChecker editSeries(String seriesLabel) {
    Button button = getPanel().getPanel("seriesCard").getButton("editSeries:" + seriesLabel);
    return SeriesEditionDialogChecker.open(button);
  }

  public SeriesEditionDialogChecker editSeries() {
    return SeriesEditionDialogChecker.open(getEditSeriesButton());
  }

  private Button getEditSeriesButton() {
    return getPanel().getButton("editSeries");
  }

  public void checkProjectCreationHidden() {
    checkComponentVisible(getPanel(), JButton.class, Lang.get("projectView.create"), false);
  }

  public void checkProjectCreationShown() {
    checkComponentVisible(getPanel(), JButton.class, Lang.get("projectView.create"), true);
  }

  public void createProject() {
    getPanel().getButton(Lang.get("projectView.create")).click();
  }

  public void editProject(String seriesLabel) {
    getPanel().getPanel("seriesCard").getButton("editSeries:" + seriesLabel).click();
  }

  public CategorizationChecker checkTable(Object[][] content) {
    Table table = getTable();
    int labelColumnIndex = isReconciliationShown(table) ? 3 : 2;
    int amountColumnIndex = isReconciliationShown(table) ? 4 : 3;
    for (Object[] objects : content) {
      objects[labelColumnIndex] = ((String) objects[labelColumnIndex]).toUpperCase();
      objects[amountColumnIndex] = Formatting.toString((Double) objects[amountColumnIndex]);
    }
    assertTrue(table.contentEquals(content));
    return this;
  }

  public CategorizationChecker checkTableIsEmpty() {
    assertTrue(getTable().isEmpty());
    return this;
  }

  public CategorizationChecker checkTableContent(final String expectedContent) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        TablePrinter printer = new TablePrinter();
        Table table = getTable();
        for (int row = 0; row < table.getRowCount(); row++) {
          Object[] rowContent = new Object[table.getColumnCount()];
          for (int col = 0; col < table.getColumnCount(); col++) {
            rowContent[col] = table.getContentAt(row, col);
          }
          printer.addRow(rowContent);
        }
        Assert.assertEquals(expectedContent.trim(), printer.toString().trim());
      }
    });
    return this;
  }

  public CategorizationChecker checkTableContains(String label) {
    Table table = getTable();
    if (!table.containsRow(2, label).isTrue()) {
      Assert.fail("Could not find row with: " + label +
                  "\nActual content:\n" +
                  CategorizationTableChecker.getCode(table));
    }
    return this;
  }

  public CategorizationChecker checkNoSelectedTableRows() {
    assertTrue(getTable().selectionIsEmpty());
    return this;
  }

  public CategorizationChecker checkSelectedTableRow(String label) {
    int index = getTable().getRowIndex(2, label);
    if (index < 0) {
      Assert.fail("Transaction " + label + " not found. Actual content:\n" + getTable().toString());
    }
    checkSelectedTableRows(index);
    return this;
  }

  public CategorizationChecker checkSelectedTableRow(int row) {
    checkSelectedTableRows(row);
    return this;
  }

  public CategorizationChecker checkSelectedTableRows(int... rows) {
    assertTrue(getTable().rowsAreSelected(rows));
    return this;
  }

  public CategorizationChecker checkSelectedTableRows(String... labels) {
    SortedSet<Integer> rows = new TreeSet<Integer>();
    for (String label : labels) {
      int index = getTable().getRowIndex(2, label);
      rows.add(index);
    }
    int[] array = new int[rows.size()];
    int index = 0;
    for (Integer row : rows) {
      array[index++] = row;
    }
    checkSelectedTableRows(array);
    return this;
  }

  public CategorizationChecker checkNoTransactionSelected() {
    assertTrue(getTable().selectionIsEmpty());
    return this;
  }

  public CategorizationChecker doubleClickTableRow(String label) {
    return doubleClickTableRow(getRowIndex(label));
  }

  public CategorizationChecker selectTransaction(String label) {
    final int index = getRowIndex(label);
    if (index < 0) {
      Assert.fail("Transaction " + label + " not found. Actual content:\n" + getTable().toString());
    }
    getTable().selectRow(index);
    return this;
  }

  public CategorizationChecker selectTransactions(String... labels) {
    String[] upperCaseLabels = new String[labels.length];
    for (int i = 0; i < upperCaseLabels.length; i++) {
      upperCaseLabels[i] = labels[i].toUpperCase();
    }
    getTable().selectRowsWithText(LABEL_COLUMN_INDEX, upperCaseLabels);
    return this;
  }

  private int getRowIndex(String label) {
    return getTable().getRowIndex(LABEL_COLUMN_INDEX, label.toUpperCase());
  }

  public CategorizationChecker doubleClickTableRow(int row) {
    getTable().doubleClick(row, 0);
    return this;
  }

  public CategorizationChecker selectNoTableRow() {
    getTable().clearSelection();
    return this;
  }

  public CategorizationChecker selectAllTransactions() {
    getTable().selectAllRows();
    return this;
  }

  public CategorizationChecker selectTableRow(int row) {
    if (row < 0) {
      Assert.fail("Row not found. Actual content:\n" + getTable().toString());
    }
    selectTableRows(row);
    return this;
  }

  public CategorizationChecker selectTableRows(int... rows) {
    getTable().selectRows(rows);
    return this;
  }

  public CategorizationChecker unselectAllTransactions() {
    getTable().clearSelection();
    return this;
  }

  public void setUseDisplayedDates() {
    this.useDisplayedDates = true;
  }

  public Table getTable() {
    views.selectCategorization();
    Table table = getSelectionPanel().getTable("transactionsToCategorize");
    int offset = 0;
    if (isReconciliationShown(table)) {
      offset = 1;
      table.setCellValueConverter(0, new ReconciliationAnnotationCellConverter());
    }
    table.setCellValueConverter(offset, new DateCellConverter(useDisplayedDates));
    table.setCellValueConverter(2 + offset, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob) modelObject;
        String label = transaction.get(Transaction.LABEL);
        if (Transaction.isToReconcile(transaction)) {
          label = "[R] " + label;
        }
        return label;
      }
    });
    table.setCellValueConverter(3 + offset, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob) modelObject;
        return Formatting.toString(transaction.get(Transaction.AMOUNT));
      }
    });
    return table;
  }

  private boolean isReconciliationShown(Table table) {
    return table.getColumnCount() > 4;
  }

  private TextBox getTransactionLabel() {
    return getPanel().getTextBox("userLabel");
  }

  public void setExceptionalIncome(String label, String seriesName, boolean createSeries) {
    int[] indices = getRowIndices(label);
    boolean first = createSeries;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        selectIncome().createSeries()
          .setName(seriesName)
          .setRepeatIrregular()
          .validate();
      }
      else {
        selectIncome().selectSeries(seriesName);
      }
      first = false;
    }
  }

  public CategorizationChecker setNewIncome(String label, String seriesName) {
    return setNewIncome(label, seriesName, null, null);
  }

  public CategorizationChecker setNewIncome(String label, String seriesName, String targetAccount) {
    return setNewIncome(label, seriesName, null, targetAccount);
  }

  public CategorizationChecker setNewIncome(String label, String seriesName, Double amount) {
    return setNewIncome(label, seriesName, amount, null);
  }

  public CategorizationChecker setNewIncome(String label, String seriesName, Double amount, String targetAccount) {
    setNewSeries(BudgetArea.INCOME, label, seriesName, amount, targetAccount);
    return this;
  }

  public CategorizationChecker setIncome(String label, String seriesName) {
    int[] rows = getRowIndices(label);
    selectTableRows(rows);
    selectIncome().selectSeries(seriesName);
    return this;
  }

  public CategorizationChecker setNewRecurring(String label, String seriesName) {
    return setNewRecurring(label, seriesName, null, null);
  }

  public CategorizationChecker setNewRecurring(String label, String seriesName, Double amount) {
    setNewRecurring(label, seriesName, amount, null);
    return this;
  }

  public CategorizationChecker setNewRecurring(String label, String seriesName, Double amount, String targetAccount) {
    setNewSeries(BudgetArea.RECURRING, label, seriesName, amount, targetAccount);
    return this;
  }

  public CategorizationChecker setNewRecurring(int row, String seriesName) {
    selectTableRow(row);
    selectRecurring().selectNewSeries(seriesName);
    return this;
  }

  public CategorizationChecker setRecurring(int row, String seriesName) {
    selectTableRow(row);
    selectRecurring().selectSeries(seriesName);
    return this;
  }

  public CategorizationChecker setRecurring(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    for (int index : indices) {
      selectTableRow(index);
      selectRecurring().selectSeries(seriesName);
    }
    return this;
  }

  public CategorizationChecker setNewVariable(String label, String seriesName) {
    return setNewVariable(label, seriesName, null, null);
  }

  public CategorizationChecker setNewVariable(String label, String seriesName, Double amount) {
    return setNewVariable(label, seriesName, amount, null);
  }

  public CategorizationChecker setNewVariable(String label, String seriesName, Double amount, String targetAccount) {
    setNewSeries(BudgetArea.VARIABLE, label, seriesName, amount, targetAccount);
    return this;
  }

  private void setNewSeries(BudgetArea budgetArea, String label, String seriesName, Double amount, String targetAccount) {
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        SeriesEditionDialogChecker editionDialog = selectAndReturn(budgetArea)
          .createSeries()
          .setName(seriesName);
        if (amount != null) {
          editionDialog.selectAllMonths();
          if (amount < 0) {
            editionDialog.selectNegativeAmounts();
            editionDialog.checkNegativeAmountsSelected();
          }
          else {
            editionDialog.selectPositiveAmounts();
            editionDialog.checkPositiveAmountsSelected();
          }
          editionDialog
            .setAmount(Math.abs(amount));
        }
        if (targetAccount != null) {
          editionDialog.setTargetAccount(targetAccount);
        }
        editionDialog.validate();
        first = false;
      }
      else {
        selectAndReturn(budgetArea).selectSeries(seriesName);
      }
    }
  }

  public CategorizationChecker setNewVariable(int row, String seriesName) {
    selectTableRow(row);
    selectVariable().selectNewSeries(seriesName);
    return this;
  }

  public CategorizationChecker setNewVariable(int row, String seriesName, double amount) {
    selectTableRow(row);
    selectVariable().selectNewSeries(seriesName, amount);
    return this;
  }

  public CategorizationChecker setVariable(int row, String seriesName) {
    selectTableRow(row);
    selectVariable().selectSeries(seriesName);
    return this;
  }

  public CategorizationChecker setVariable(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    for (int index : indices) {
      selectTableRow(index);
      selectVariable().selectSeries(seriesName);
    }
    return this;
  }

  public CategorizationChecker setVariable(String label, String seriesName, String subSeriesName) {
    int[] indices = getRowIndices(label);
    for (int index : indices) {
      selectTableRow(index);
      selectVariable().selectSubSeries(seriesName, subSeriesName);
    }
    return this;
  }

  public CategorizationChecker setNewExtra(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        selectExtras()
          .createSeries(seriesName)
          .selectSeries(seriesName);
        first = false;
      }
      else {
        selectExtras().selectSeries(seriesName);
      }
    }
    return this;
  }

  public CategorizationChecker setExtra(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    for (int index : indices) {
      selectTableRow(index);
      selectExtras().selectSeries(seriesName);
    }
    return this;
  }

  int[] getRowIndices(String label) {
    int[] index = getTable().getRowIndices(LABEL_COLUMN_INDEX, label.toUpperCase());
    if (index.length <= 0) {
      Assert.fail("Label '" + label + "' not found - actual table content:\n" + getTable().toString());
    }
    return index;
  }

  public CategorizationChecker setTransfer(String label, String seriesName) {
    for (int index : getRowIndices(label)) {
      setTransfer(index, seriesName);
    }
    return this;
  }

  public CategorizationChecker setNewTransfer(String label, String seriesName, String fromAccount, String toAccount) {
    boolean first = true;
    for (int index : getRowIndices(label)) {
      if (first) {
        selectTableRows(index);
        selectTransfers().createSeries()
          .setName(seriesName)
          .setFromAccount(fromAccount)
          .setToAccount(toAccount)
          .validate();
        first = false;
      }
      else {
        setTransfer(index, seriesName);
      }
    }
    return this;
  }

  private CategorizationChecker setTransfer(int rowIndex, String seriesName) {
    selectTableRows(rowIndex);
    selectTransfers().selectSeries(seriesName);
    return this;
  }

  public CategorizationGaugeChecker getCompletionGauge() {
    return new CategorizationGaugeChecker(mainWindow);
  }

  public CategorizationTableChecker initContent() {
    return new CategorizationTableChecker(getTable());
  }

  public CategorizationChecker checkCustomFilterVisible(boolean visible) {
    assertEquals(visible, getSelectionPanel().getPanel("customFilter").isVisible());
    return this;
  }

  public void clearCustomFilter() {
    getSelectionPanel().getPanel("customFilter").getButton().click();
  }

  public CategorizationChecker selectUncategorized() {
    selectBudgetArea(BudgetArea.UNCATEGORIZED);
    return this;
  }

  public void setUncategorized(int row) {
    selectTableRow(row);
    selectUncategorized().setUncategorized();
  }

  public CategorizationChecker setUncategorized() {
    selectUncategorized();
    getPanel().getButton("uncategorizeSelected").click();
    return this;
  }

  public void checkShowsAllTransactions() {
    checkTransactionFilterMode(CategorizationFilteringMode.ALL);
  }

  public void checkShowsSelectedMonthsOnly() {
    checkTransactionFilterMode(CategorizationFilteringMode.SELECTED_MONTHS);
  }

  public void checkShowsUncategorizedTransactionsOnly() {
    checkTransactionFilterMode(CategorizationFilteringMode.UNCATEGORIZED);
  }

  public void checkShowsUncategorizedTransactionsForSelectedMonths() {
    checkTransactionFilterMode(CategorizationFilteringMode.UNCATEGORIZED_SELECTED_MONTHS);
  }

  public void checkShowsToReconcile() {
    checkTransactionFilterMode(CategorizationFilteringMode.TO_RECONCILE);
  }

  private void checkTransactionFilterMode(final CategorizationFilteringMode mode) {
    assertThat(getSelectionPanel().getComboBox("transactionFilterCombo").selectionEquals(mode.toString()));
  }

  public void showAllTransactions() {
    selectTransactionFilterMode(CategorizationFilteringMode.ALL);
  }

  public CategorizationChecker showSelectedMonthsOnly() {
    selectTransactionFilterMode(CategorizationFilteringMode.SELECTED_MONTHS);
    return this;
  }

  public CategorizationChecker showUncategorizedTransactionsForSelectedMonths() {
    selectTransactionFilterMode(CategorizationFilteringMode.UNCATEGORIZED_SELECTED_MONTHS);
    return this;
  }

  public CategorizationChecker showUnreconciledOnly() {
    selectTransactionFilterMode(CategorizationFilteringMode.MISSING_RECONCILIATION_ANNOTATION);
    return this;
  }

  public void checkFilteringModes(String... entries) {
    assertThat(getSelectionPanel().getComboBox("transactionFilterCombo").contentEquals(entries));
  }

  private void selectTransactionFilterMode(CategorizationFilteringMode mode) {
    getSelectionPanel().getComboBox("transactionFilterCombo").select(mode.toString());
  }

  public void showLastImportedFileOnly() {
    selectTransactionFilterMode(CategorizationFilteringMode.LAST_IMPORTED_FILE);
  }

  public void showUncategorizedTransactionsOnly() {
    selectTransactionFilterMode(CategorizationFilteringMode.UNCATEGORIZED);
  }

  public CategorizationChecker checkFirstCategorizationSignpostDisplayed(String message) {
    checkSignpostVisible(getSelectionPanel(), getTable(), message);
    return this;
  }

  public CategorizationChecker checkAreaSelectionSignpostDisplayed(String message) {
    checkSignpostVisible(mainWindow, getPanel().getPanel("budgetAreaSelectionPanel"), message);
    return this;
  }

  public CategorizationChecker checkSkipMessageHidden() {
    checkComponentVisible(getSelectionPanel(), JPanel.class, "skipCategorizationPanel", false);
    return this;
  }

  public CategorizationChecker checkSkipMessageDisplayed() {
    checkComponentVisible(getSelectionPanel(), JPanel.class, "skipCategorizationPanel", true);
    return this;
  }

  public CategorizationChecker skipAndCloseSignpostDialog() {
    TextBox message = getSelectionPanel().getPanel("skipCategorizationPanel").getTextBox("skipCategorizationMessage");
    SignpostDialogChecker
      .open(message.triggerClickOnHyperlink("click"))
      .close();
    return this;
  }

  public void checkTableBackground(String... colors) {
    Table table = getTable();
    assertThat(table.rowCountEquals(colors.length));

    String[][] cellColors = new String[table.getRowCount()][table.getColumnCount()];
    for (int i = 0; i < colors.length; i++) {
      String color = colors[i];
      Arrays.fill(cellColors[i], color);
    }

    assertThat(table.backgroundEquals(cellColors));
  }

  public ConfirmationDialogChecker delete(String label) {
    int row = getRowIndex(label.toUpperCase());
    Assert.assertTrue(label + " not found", row >= 0);
    return delete(row);
  }

  public ConfirmationDialogChecker delete(int... row) {
    getTable().selectRows(row);
    Window deleteDialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        KeyUtils.pressKey(getTable(), Key.DELETE);
      }
    });
    return new ConfirmationDialogChecker(deleteDialog);
  }

  public CategorizationChecker checkCategorizationShown() {
    checkComponentVisible(getPanel(), JPanel.class, "reconciliationPanel", false);
    checkComponentVisible(getPanel(), JPanel.class, "seriesPanel", true);
    return this;
  }

  public CategorizationChecker checkReconciliationShown() {
    checkComponentVisible(getPanel(), JPanel.class, "reconciliationPanel", true);
    checkComponentVisible(getPanel(), JPanel.class, "seriesPanel", false);
    return this;
  }

  public CategorizationChecker checkReconciliationWarningHidden() {
    checkComponentVisible(getPanel(), JPanel.class, "reconciliationWarningPanel", false);
    return this;
  }

  public CategorizationChecker checkReconciliationWarningShown(String text) {
    checkComponentVisible(getSelectionPanel(), JPanel.class, "reconciliationWarningPanel", true);
    assertTrue(getSelectionPanel().getPanel("reconciliationWarningPanel")
                 .getTextBox("message").textEquals(text));
    return this;
  }

  public CategorizationChecker clickReconciliationWarningButton(String link) {
    getSelectionPanel().getPanel("reconciliationWarningPanel").getTextBox("message").clickOnHyperlink(link);
    return this;
  }

  public ReconciliationChecker getReconciliation() {
    return new ReconciliationChecker(getPanel().getPanel("reconciliationPanel"));
  }

  public CategorizationChecker checkSwitchToReconciliationLinkShown() {
    Panel panel = getPanel().getPanel("reconciliationNavigationPanel");
    checkComponentVisible(panel, JButton.class, "switchToReconciliation", true);
    checkComponentVisible(panel, JButton.class, "switchToCategorization", false);
    return this;
  }

  public ReconciliationChecker switchToReconciliation() {
    Panel panel = getPanel().getPanel("reconciliationNavigationPanel");
    panel.getButton("switchToReconciliation").click();
    return getReconciliation();
  }

  public CategorizationChecker checkSwitchToCategorizationLinkShown() {
    Panel panel = getPanel().getPanel("reconciliationNavigationPanel");
    checkComponentVisible(panel, JButton.class, "switchToReconciliation", false);
    checkComponentVisible(panel, JButton.class, "switchToCategorization", true);
    return this;
  }

  public CategorizationChecker switchToCategorization() {
    Panel panel = getPanel().getPanel("reconciliationNavigationPanel");
    panel.getButton("switchToCategorization").click();
    return this;
  }

  public CategorizationChecker checkReconciliationSwitchLinksHidden() {
    Panel panel = getPanel().getPanel("reconciliationNavigationPanel");
    checkComponentVisible(panel, JButton.class, "switchToReconciliation", false);
    checkComponentVisible(panel, JButton.class, "switchToCategorization", false);
    return this;
  }

  public void checkQuasiCompleteProgressMessageShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Categorization is quasi complete. You can now see the Budget page.");
  }

  public void checkGotoBudgetSignpostShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Categorization is completed. You can now see the Budget page.");
  }

  public void checkSkipAndGotoBudgetSignpostShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Go to the Budget page to continue.");
  }

  private ToggleButton getBudgetToggle() {
    return mainWindow.getToggleButton("budgetCardToggle");
  }
}