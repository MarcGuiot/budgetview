package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.gui.categorization.components.CategorizationFilteringMode;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.KeyUtils;

import javax.swing.AbstractButton;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class CategorizationChecker extends ViewChecker {
  public static final int LABEL_COLUMN_INDEX = 2;
  public static final int AMOUNT_COLUMN_INDEX = 3;
  private Panel panel;

  public CategorizationChecker(Window mainWindow) {
    super(mainWindow);
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
    assertThat(panel.getTextBox("noSelectionMessage").textContains("You must select an operation"));
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

  public SavingsCategorizationChecker selectSavings() {
    return new SavingsCategorizationChecker(this, BudgetArea.SAVINGS);
  }

  public OtherCategorizationChecker selectOther() {
    selectBudgetArea(BudgetArea.OTHER);
    return new OtherCategorizationChecker(getPanel(BudgetArea.OTHER), this);
  }

  public AccountEditionChecker createAccount() {
    return AccountEditionChecker.open(getPanel().getButton("New account").triggerClick());
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

  public CategorizationChecker checkAllButSavingBudgetAreaAreDisable() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area == BudgetArea.ALL) {
        continue;
      }
      if (area == BudgetArea.SAVINGS || area == BudgetArea.UNCATEGORIZED) {
        assertTrue(getPanel().getToggleButton(area.getName()).isEnabled());
      }
      else {
        assertFalse(getPanel().getToggleButton(area.getName()).isEnabled());
      }
    }
    return this;
  }

  public void checkAllBudgetAreaAreEnable() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area == BudgetArea.ALL) {
        continue;
      }
      assertTrue(getPanel().getToggleButton(area.getName()).isEnabled());
    }
  }

  public CategorizationChecker checkMultipleSeriesSelection() {
    assertThat(getPanel().getPanel("seriesCard").getInputTextBox().textContains("Multiple Series selection"));
    return this;
  }

  public CategorizationChecker checkSavingPreSelected() {
    assertThat(getPanel().getToggleButton(BudgetArea.SAVINGS.getName()).isSelected());
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

  public class SavingsCategorizationChecker extends BudgetAreaCategorizationChecker {
    private SavingsCategorizationChecker(CategorizationChecker categorizationChecker, BudgetArea budgetArea) {
      super(categorizationChecker, budgetArea);
    }

    public SavingsCategorizationChecker selectAndCreateSavingsSeries(String savingsName, String fromAccount) {
      selectSavings().createSeries()
        .setName(savingsName)
        .setFromAccount(fromAccount)
        .validate();
      return this;
    }

    public SavingsCategorizationChecker selectAndCreateSavingsSeries(String savingsName, String fromAccount, String toAccount) {
      selectSavings().createSeries()
        .setName(savingsName)
        .setFromAccount(fromAccount)
        .setToAccount(toAccount)
        .validate();
      return this;
    }

    public AccountEditionChecker createSavingsAccount() {
      selectSavings();
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

  public BudgetAreaCategorizationChecker getExtras() {
    return checkSelectedAndReturn(BudgetArea.EXTRAS);
  }

  public BudgetAreaCategorizationChecker getSavings() {
    return checkSelectedAndReturn(BudgetArea.SAVINGS);
  }

  public CategorizationChecker checkToCategorize() {
    assertThat(getPanel().getToggleButton(BudgetArea.UNCATEGORIZED.getName()).isSelected());
    return this;
  }

  private BudgetAreaCategorizationChecker checkSelectedAndReturn(BudgetArea budgetArea) {
    assertTrue(getPanel().getToggleButton(budgetArea.getName()).isSelected());
    return new BudgetAreaCategorizationChecker(this, budgetArea);
  }

  public CategorizationChecker checkRecurringSeriesIsSelected(String seriesName) {
    return checkSeriesIsSelected(BudgetArea.RECURRING, seriesName);
  }

  public CategorizationChecker checkSavingsSeriesIsSelected(String seriesName) {
    return checkSeriesIsSelected(BudgetArea.SAVINGS, seriesName);
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

  Button getEditSeriesButton() {
    return getPanel().getButton("editSeries");
  }

  public CategorizationChecker checkTable(Object[][] content) {
    for (Object[] objects : content) {
      objects[2] = ((String)objects[2]).toUpperCase();
    }
    assertTrue(getTable().contentEquals(content));
    return this;
  }

  public CategorizationChecker checkTableIsEmpty() {
    assertTrue(getTable().isEmpty());
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

  public Table getTable() {
    views.selectCategorization();
    Table table = getPanel().getTable("transactionsToCategorize");
    table.setCellValueConverter(0, new DateCellConverter());
    table.setCellValueConverter(3, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob)modelObject;
        return transaction.get(Transaction.AMOUNT);
      }
    });
    return table;
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
          .setIrregular()
          .validate();
      }
      else {
        selectIncome().selectSeries(seriesName);
      }
      first = false;
    }
  }

  public CategorizationChecker setNewIncome(String label, String seriesName) {
    return setNewIncome(label, seriesName, null);
  }

  public CategorizationChecker setNewIncome(String label, String seriesName, Double amount) {
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        SeriesEditionDialogChecker editionDialogChecker = selectIncome()
          .createSeries()
          .setName(seriesName);
        if (amount != null) {
          editionDialogChecker
            .selectAllMonths()
            .setAmount(amount);
        }
        editionDialogChecker.validate();
//          .selectSeries(seriesName);
        first = false;
      }
      else {
        selectIncome().selectSeries(seriesName);
      }
    }
    return this;
  }

  public CategorizationChecker setIncome(String label, String seriesName) {
    int[] rows = getRowIndices(label);
    selectTableRows(rows);
    selectIncome().selectSeries(seriesName);
    return this;
  }

  public CategorizationChecker setNewRecurring(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        selectRecurring()
          .createSeries(seriesName)
          .selectSeries(seriesName);
        first = false;
      }
      else {
        selectRecurring().selectSeries(seriesName);
      }
    }
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
    return setNewVariable(label, seriesName, null);
  }

  public CategorizationChecker setNewVariable(String label, String seriesName, Double amount) {
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        SeriesEditionDialogChecker editionDialogChecker = selectVariable()
          .createSeries()
          .setName(seriesName);
        if (amount != null) {
          editionDialogChecker.selectAllMonths();
          if (amount < 0) {
            editionDialogChecker.selectNegativeAmounts();
            editionDialogChecker.checkNegativeAmountsSelected();
          }
          else {
            editionDialogChecker.selectPositiveAmounts();
            editionDialogChecker.checkPositiveAmountsSelected();
          }
          editionDialogChecker
            .setAmount(Math.abs(amount));
        }
        editionDialogChecker
          .validate();
        first = false;
      }
      else {
        selectVariable().selectSeries(seriesName);
      }
    }
    return this;
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

  public CategorizationChecker setSavings(String label, String seriesName) {
    for (int index : getRowIndices(label)) {
      setSavings(index, seriesName);
    }
    return this;
  }

  public CategorizationChecker setNewSavings(String label, String seriesName, String fromAccount, String toAccount) {
    boolean first = true;
    for (int index : getRowIndices(label)) {
      if (first) {
        selectTableRows(index);
        selectSavings().createSeries()
          .setName(seriesName)
          .setFromAccount(fromAccount)
          .setToAccount(toAccount)
          .validate();
        first = false;
      }
      else {
        setSavings(index, seriesName);
      }
    }
    return this;
  }

  private CategorizationChecker setSavings(int rowIndex, String seriesName) {
    selectTableRows(rowIndex);
    selectSavings().selectSeries(seriesName);
    return this;
  }

  public CategorizationGaugeChecker getCompletionGauge() {
    return new CategorizationGaugeChecker(mainWindow);
  }

  public CategorizationTableChecker initContent() {
    return new CategorizationTableChecker();
  }

  public CategorizationChecker checkCustomFilterVisible(boolean visible) {
    assertEquals(visible, getPanel().getPanel("customFilter").isVisible());
    return this;
  }

  public void clearCustomFilter() {
    getPanel().getPanel("customFilter").getButton().click();
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
    checkTransctionFilterMode(CategorizationFilteringMode.ALL);
  }

  public void checkShowsSelectedMonthsOnly() {
    checkTransctionFilterMode(CategorizationFilteringMode.SELECTED_MONTHS);
  }

  public void checkShowsUncategorizedTransactionsOnly() {
    checkTransctionFilterMode(CategorizationFilteringMode.UNCATEGORIZED);
  }

  public void checkShowsUncategorizedTransactionsForSelectedMonths() {
    checkTransctionFilterMode(CategorizationFilteringMode.UNCATEGORIZED_SELECTED_MONTHS);
  }

  private void checkTransctionFilterMode(final CategorizationFilteringMode mode) {
    assertThat(getPanel().getComboBox("transactionFilterCombo").selectionEquals(mode.toString()));
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

  private void selectTransactionFilterMode(CategorizationFilteringMode mode) {
    getPanel().getComboBox("transactionFilterCombo").select(mode.toString());
  }

  public void showLastImportedFileOnly() {
    selectTransactionFilterMode(CategorizationFilteringMode.LAST_IMPORTED_FILE);
  }

  public void showUncategorizedTransactionsOnly() {
    selectTransactionFilterMode(CategorizationFilteringMode.UNCATEGORIZED);
  }


  public CategorizationChecker checkSelectionSignpostDisplayed(String message) {
    checkSignpostVisible(getPanel(), getTable(), message);
    return this;
  }

  public CategorizationChecker checkFirstCategorizationSignpostDisplayed(String message) {
    checkSignpostVisible(getPanel(), getTable(), message);
    return this;
  }

  public CategorizationChecker checkAreaSelectionSignpostDisplayed(String message) {
    checkSignpostVisible(mainWindow, getPanel().getPanel("budgetAreaSelectionPanel"), message);
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

  public class CategorizationTableChecker extends TableChecker {

    private CategorizationTableChecker() {
    }

    public CategorizationTableChecker add(String date, String series, String label, double amount) {
      add(new Object[]{date, series, convertLabel(label), amount});
      return this;
    }

    private String convertLabel(String label) {
      if (!label.startsWith("Planned")) {
        return label.toUpperCase();
      }
      return label;
    }

    protected Table getTable() {
      return CategorizationChecker.this.getTable();
    }
  }

  public void checkQuasiCompleteProgressMessageShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Categorization is quasi complete. You can now see the Budget page.");
  }

  public void checkCompleteProgressMessageShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Categorization is completed. You can now see the Budget page.");
  }

  private ToggleButton getBudgetToggle() {
    return mainWindow.getToggleButton("budgetCardToggle");
  }
}