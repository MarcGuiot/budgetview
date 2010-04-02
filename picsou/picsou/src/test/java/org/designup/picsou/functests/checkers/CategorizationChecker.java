package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.gui.categorization.components.TransactionFilteringMode;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
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

public class CategorizationChecker extends GuiChecker {
  private Window mainWindow;
  public static final int LABEL_COLUMN_INDEX = 2;
  public static final int AMOUNT_COLUMN_INDEX = 3;

  public CategorizationChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  Panel getPanel() {
    return mainWindow.getPanel("categorizationView");
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
    return getPanel().getPanel("budgetAreaToggles");
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

  public BudgetAreaCategorizationChecker selectEnvelopes() {
    return selectAndReturn(BudgetArea.ENVELOPES);
  }

  public BudgetAreaCategorizationChecker selectExtras() {
    return selectAndReturn(BudgetArea.EXTRAS);
  }

  public SavingsCategorizationChecker selectSavings() {
    return new SavingsCategorizationChecker(this, BudgetArea.SAVINGS);
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

  public void checkEditSeriesNotVisitble(String seriesLabel) {
    Button button = getPanel().getPanel("seriesCard").getButton("editSeries:" + seriesLabel);
    assertFalse(button.isVisible());
  }

  public void checkUserDate(TransactionDetailsChecker details, String yyyyMMdd, String label) {
    selectTransaction(label);
    details.checkBudgetDate(yyyyMMdd);
  }

  public CategorizationChecker checkAllButSavingBudgetAreaAreDisable() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area == BudgetArea.ALL){
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
      if (area == BudgetArea.ALL){
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

  public CategorizationChecker checkEnvelopesPreSelected() {
    assertThat(getPanel().getToggleButton(BudgetArea.ENVELOPES.getName()).isSelected());
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

  public BudgetAreaCategorizationChecker getEnvelopes() {
    return checkSelectedAndReturn(BudgetArea.ENVELOPES);
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
    return SeriesEditionDialogChecker.open(getPanel().getButton("editSeries"));
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
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        selectIncome()
          .createSeries(seriesName)
          .selectSeries(seriesName);
        first = false;
      }
      else {
        selectIncome().selectSeries(seriesName);
      }
    }
    return this;
  }


  public CategorizationChecker setNewIncome(int row, String seriesName) {
    selectTableRow(row);
    selectIncome().selectNewSeries(seriesName);
    return this;
  }

  public CategorizationChecker setIncome(int row, String seriesName) {
    selectTableRow(row);
    selectIncome().selectSeries(seriesName);
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

  public CategorizationChecker setNewEnvelope(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    boolean first = true;
    for (int index : indices) {
      selectTableRow(index);
      if (first) {
        selectEnvelopes()
          .createSeries(seriesName);
        first = false;
      }
      else {
        selectEnvelopes().selectSeries(seriesName);
      }
    }
    return this;
  }

  public CategorizationChecker setNewEnvelope(int row, String seriesName) {
    selectTableRow(row);
    selectEnvelopes().selectNewSeries(seriesName);
    return this;
  }

  public CategorizationChecker setEnvelope(int row, String seriesName) {
    selectTableRow(row);
    selectEnvelopes().selectSeries(seriesName);
    return this;
  }

  public CategorizationChecker setEnvelope(String label, String seriesName) {
    int[] indices = getRowIndices(label);
    for (int index : indices) {
      selectTableRow(index);
      selectEnvelopes().selectSeries(seriesName);
    }
    return this;
  }

  public CategorizationChecker setEnvelope(String label, String seriesName, String subSeriesName) {
    int[] indices = getRowIndices(label);
    for (int index : indices) {
      selectTableRow(index);
      selectEnvelopes().selectSubSeries(seriesName, subSeriesName);
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

  public CategorizationChecker setNewExtra(int row, String seriesName) {
    selectTableRow(row);
    selectExtras().selectNewSeries(seriesName);
    return this;
  }

  public CategorizationChecker setExtra(int row, String seriesName) {
    selectTableRow(row);
    selectExtras().selectSeries(seriesName);
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
    return new CategorizationGaugeChecker(getPanel());
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

  public void setUncategorized(String label) {
    selectTableRows(getRowIndices(label));
    setUncategorized();
  }

  public CategorizationChecker setUncategorized() {
    selectUncategorized();
    getPanel().getButton("uncategorizeSelected").click();
    return this;
  }

  public HelpChecker clickHelpLink(String linkText) {
    JEditorPane editorPane = getPanel().getPanel("series").findSwingComponent(JEditorPane.class);
    TextBox textBox = new TextBox(editorPane);
    return HelpChecker.open(textBox.triggerClickOnHyperlink(linkText));
  }

  public void checkShowsAllTransactions() {
    checkTransctionFilterMode(TransactionFilteringMode.ALL);
  }

  public void checkShowsSelectedMonthsOnly() {
    checkTransctionFilterMode(TransactionFilteringMode.SELECTED_MONTHS);
  }

  public void checkShowsUncategorizedTransactionsOnly() {
    checkTransctionFilterMode(TransactionFilteringMode.UNCATEGORIZED);
  }

  public void checkShowsLastImportedTransaction() {
    checkTransctionFilterMode(TransactionFilteringMode.LAST_IMPORTED_FILE);
  }

  private void checkTransctionFilterMode(final TransactionFilteringMode mode) {
    assertThat(getPanel().getComboBox("transactionFilterCombo").selectionEquals(mode.toString()));
  }

  public void showAllTransactions() {
    selectTransactionFilterMode(TransactionFilteringMode.ALL);
  }

  public CategorizationChecker showSelectedMonthsOnly() {
    selectTransactionFilterMode(TransactionFilteringMode.SELECTED_MONTHS);
    return this;
  }

  public CategorizationChecker showRecentUncategorizedTransactionsOnly() {
    selectTransactionFilterMode(TransactionFilteringMode.UNCATEGORIZED_LAST_THREE_MONTHS);
    return this;
  }

  private void selectTransactionFilterMode(TransactionFilteringMode mode) {
    getPanel().getComboBox("transactionFilterCombo").select(mode.toString());
  }

  public void showLastImportedFileOnly() {
    selectTransactionFilterMode(TransactionFilteringMode.LAST_IMPORTED_FILE);
  }

  public void showUncategorizedTransactionsOnly() {
    selectTransactionFilterMode(TransactionFilteringMode.UNCATEGORIZED);
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

    public CategorizationTableChecker add(String date, TransactionType prelevement, String label, String note, double amount, String series) {
      if (!label.startsWith("Planned")) {
        label = label.toUpperCase();
      }
      add(new Object[]{date, series, label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, TransactionType prelevement, String label, String note, double amount) {
      if (!label.startsWith("Planned")) {
        label = label.toUpperCase();
      }
      add(new Object[]{date, "", label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, String label, double amount) {
      if (!label.startsWith("Planned")) {
        label = label.toUpperCase();
      }
      add(new Object[]{date, "", label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, String series, String label, double amount) {
      if (!label.startsWith("Planned")) {
        label = label.toUpperCase();
      }
      add(new Object[]{date, series, label, amount});
      return this;
    }

    protected Table getTable() {
      return CategorizationChecker.this.getTable();
    }
  }
}