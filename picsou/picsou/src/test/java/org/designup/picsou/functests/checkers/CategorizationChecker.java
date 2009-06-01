package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.gui.categorization.components.TransactionFilteringMode;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.utils.KeyUtils;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.AbstractButton;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategorizationChecker extends GuiChecker {
  private Window mainWindow;
  public static final int LABEL_COLUMN_INDEX = 2;
  public static final int AMOUNT_COLUMN_INDEX = 3;

  public CategorizationChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  private Panel getPanel() {
    return mainWindow.getPanel("categorizationView");
  }

  private void selectBudgetArea(BudgetArea area) {
    AbstractButton button = getBudgetAreasPanel().findSwingComponent(AbstractButton.class, area.getName());
    if (button == null) {
      Assert.fail("No button found for budget area " + area);
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

  public CategorizationChecker checkLabel(int count) {
    assertTrue(getTransactionLabel().textEquals(count + " operations"));
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
        assertFalse(panel.containsUIComponent(Button.class, area.getName()));
        assertFalse(panel.containsUIComponent(ToggleButton.class, area.getName()));
      }
    }
    assertThat(panel.getTextBox("noSelectionMessage").textContains("You must select an operation"));
  }

  public void checkBudgetAreaIsSelected(BudgetArea budgetArea) {
    assertTrue(getPanel().getToggleButton(budgetArea.getGlob().get(BudgetArea.NAME)).isSelected());
  }

  public CategorizationChecker selectIncome() {
    selectBudgetArea(BudgetArea.INCOME);
    return this;
  }

  public CategorizationChecker checkNoIncomeSeriesDisplayed() {
    Panel seriesPanel = getIncomeSeriesPanel();
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    Assert.assertEquals(1, toggles.length);
    RadioButton invisibleSelector = (RadioButton)toggles[0];
    assertFalse(invisibleSelector.isVisible());
    return this;
  }

  public void checkContainsIncomeSeries(String... seriesNames) {
    Panel seriesPanel = getIncomeSeriesPanel();

    List<String> names = new ArrayList<String>();
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    for (UIComponent toggle : toggles) {
      names.add(toggle.getLabel());
    }

    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
  }

  public CategorizationChecker selectExceptionalIncomeSeries(String name, boolean showSeriesInitialization) {
    Panel panel = getIncomeSeriesPanel();
    if (showSeriesInitialization) {
      createIncomeSeries()
        .setName(name)
        .setCategory(MasterCategory.INCOME)
        .setIrregular()
        .validate();
    }
    else {
      panel.getToggleButton(name).click();
    }
    return this;
  }

  public CategorizationChecker selectIncomeSeries(String name, boolean showSeriesInitialization) {
    Panel panel = getIncomeSeriesPanel();
    if (showSeriesInitialization) {
      createIncomeSeries()
        .setName(name)
        .setCategory(MasterCategory.INCOME)
        .validate();
    }
    else {
      panel.getRadioButton(name).click();
    }
    return this;
  }

  private Panel getIncomeSeriesPanel() {
    Panel panel = this.getPanel().getPanel("incomeSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationChecker selectRecurring() {
    selectBudgetArea(BudgetArea.RECURRING);
    return this;
  }

  public CategorizationChecker checkContainsNoRecurringSeries() {
    return checkContainsRecurringSeries();
  }

  public CategorizationChecker checkContainsRecurringSeries(String... seriesNames) {
    Panel seriesPanel = getRecurringSeriesPanel();

    List<String> names = new ArrayList<String>();
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    for (UIComponent toggle : toggles) {
      names.add(toggle.getLabel());
    }

    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
    return this;
  }

  public CategorizationChecker checkNoRecurringSeriesSelected() {
    Panel seriesPanel = getRecurringSeriesPanel();
    UIComponent[] selectors = seriesPanel.getUIComponents(RadioButton.class);
    for (UIComponent selector : selectors) {
      if (selector.getAwtComponent().isVisible()) {
        assertFalse(selector.getLabel() + " selected", ((RadioButton)selector).isSelected());
      }
    }
    return this;
  }

  public CategorizationChecker selectNewRecurringSeries(String name, MasterCategory category,
                                                        boolean transactionWasAlreadyCategorized) {
    Panel panel = getRecurringSeriesPanel();
    createRecurringSeries()
      .setName(name)
      .setCategory(category)
      .validate();
    if (transactionWasAlreadyCategorized) {
      panel.getRadioButton(name).click();
    }
    return this;
  }

  public CategorizationChecker categorizeInRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
    panel.getRadioButton(name).click();
    return this;
  }

  public CategorizationChecker selectRecurringSeries(String name, MasterCategory category, boolean createSeries) {
    Panel panel = getRecurringSeriesPanel();
    if (createSeries) {
      createRecurringSeries()
        .setName(name)
        .setCategory(category)
        .validate();
    }
    else {
      panel.getRadioButton(name).click();
    }
    return this;
  }

  public CategorizationChecker selectRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
    panel.getRadioButton(name).click();
    return this;
  }

  private Panel getRecurringSeriesPanel() {
    Panel panel = getPanel();
    assertTrue(panel.containsUIComponent(Panel.class, "recurringSeriesChooser"));
    Panel recurringSeriesPanel = panel.getPanel("recurringSeriesChooser");
    assertTrue(recurringSeriesPanel.isVisible());
    return recurringSeriesPanel;
  }

  public CategorizationChecker checkContainsButtonInReccuring(String label) {
    Panel panel = getRecurringSeriesPanel();
    assertTrue(panel.getRadioButton(label).isEnabled());
    return this;
  }

  public CategorizationChecker checkRecurringSeriesIsSelected(String seriesName) {
    assertTrue(getPanel().getToggleButton("Recurring").isSelected());

    Panel panel = getRecurringSeriesPanel();
    assertTrue(panel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkRecurringSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(getPanel().getPanel("recurringSeriesChooser").getRadioButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkIncomeSeriesIsSelected(String seriesName) {
    assertTrue(getPanel().getToggleButton("Income").isSelected());

    Panel panel = getIncomeSeriesPanel();
    assertTrue(panel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkIncomeSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(getPanel().getPanel("incomeSeriesChooser").getRadioButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker selectEnvelopes() {
    selectBudgetArea(BudgetArea.ENVELOPES);
    return this;
  }

  public CategorizationChecker checkContainsLabelInEnvelope(String label) {
    Panel panel = getEnvelopeSeriesPanel();
    Assertion assertion = panel.containsLabel(label);
    if (!assertion.isTrue()) {
      assertTrue(panel.getRadioButton(label).isEnabled());
    }
    return this;
  }

  public CategorizationChecker checkContainsEnvelope(String envelopeName, MasterCategory... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    TextBox label = panel.getTextBox(envelopeName);
    Panel seriesPanel = label.getContainer("seriesBlock");
    for (MasterCategory category : categories) {
      assertTrue(seriesPanel.containsUIComponent(RadioButton.class, category.getName()));
    }
    return this;
  }

  public CategorizationChecker checkContainsEnvelope(String envelopeName, String... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (String category : categories) {
      assertTrue(panel.containsUIComponent(RadioButton.class, envelopeName + ":" + category));
    }
    return this;
  }

  public CategorizationChecker checkNotContainsEnvelope(String... envelopeName) {
    Panel panel = getEnvelopeSeriesPanel();
    for (String name : envelopeName) {
      assertFalse(name + " is present", panel.containsLabel(name));
    }
    return this;
  }

  public CategorizationChecker checkNotContainsCategoryInEnvelope(String envelopeName, String... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (String category : categories) {
      assertFalse(panel.containsUIComponent(RadioButton.class, envelopeName + ":" + category));
    }
    return this;
  }

  public CategorizationChecker selectEnvelopeSeries(String envelopeName, MasterCategory category, boolean createSeries) {
    Panel panel = getEnvelopeSeriesPanel();
    if (createSeries) {
      createEnvelopeSeries()
        .setName(envelopeName)
        .setCategory(category)
        .validate();
    }
    else {
      String selectorName = envelopeName + ":" + category.getName();
      panel.getRadioButton(selectorName).click();
    }
    return this;
  }

  public CategorizationChecker selectEnvelopeSeries(String envelopeName, String category) {
    Panel panel = getEnvelopeSeriesPanel();
    panel.getRadioButton(envelopeName + ":" + category).click();
    return this;
  }

  private Panel getEnvelopeSeriesPanel() {
    Panel panel = getPanel();
    assertTrue(panel.containsUIComponent(Panel.class, "envelopeSeriesChooser"));
    Panel envelopeSeriesPanel = this.getPanel().getPanel("envelopeSeriesChooser");
    assertTrue(envelopeSeriesPanel.isVisible());
    return envelopeSeriesPanel;
  }

  public CategorizationChecker selectSpecial() {
    selectBudgetArea(BudgetArea.SPECIAL);
    return this;
  }

  public CategorizationChecker selectSpecialSeries(String seriesName, MasterCategory category, boolean createSeries) {
    Panel panel = getSpecialSeriesPanel();
    String name = seriesName + ":" + category.getName();
    if (createSeries) {
      createSpecialSeries()
        .setName(seriesName)
        .setCategory(category)
        .validate();
    }
    else {
      panel.getRadioButton(name).click();
    }
    return this;
  }

  private Panel getSpecialSeriesPanel() {
    Panel panel = this.getPanel().getPanel("specialSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationChecker selectSavings() {
    selectBudgetArea(BudgetArea.SAVINGS);
    return this;
  }

  public CategorizationChecker selectSavingsSeries(String savingsName) {
    Panel panel = getSavingsSeriesPanel();
    panel.getRadioButton(savingsName).click();
    return this;
  }

  public CategorizationChecker selectAndCreateSavingsSeries(String savingsName, String fromAccount) {
    createSavingsSeries()
      .setName(savingsName)
      .setFromAccount(fromAccount)
      .setCategory(MasterCategory.SAVINGS)
      .validate();
    return this;
  }

  private Panel getSavingsSeriesPanel() {
    Panel panel = this.getPanel().getPanel("savingsSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationChecker selectOccasional() {
    selectBudgetArea(BudgetArea.OCCASIONAL);
    return this;
  }

  public CategorizationChecker selectOccasionalSeries(MasterCategory category) {
    selectOccasional();
    getOccasionalSeriesPanel().getRadioButton("occasionalSeries" + ":" + category.getName()).click();
    return this;
  }

  public CategorizationChecker selectOccasionalSeries(MasterCategory masterCategory, String subcat) {
    selectOccasional();
    final String selectorName = "occasionalSeries" + ":" + masterCategory.getName() + ":" + subcat;
    getOccasionalSeriesPanel().getRadioButton(selectorName).click();
    return this;
  }

  private Panel getOccasionalSeriesPanel() {
    Panel panel = getPanel().getPanel("occasionalSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void checkContainsOccasional(MasterCategory... categories) {
    Panel panel = getOccasionalSeriesPanel();
    for (MasterCategory category : categories) {
      assertTrue(panel.containsUIComponent(RadioButton.class, "occasionalSeries" + ":" + category.getName()));
    }
  }

  public void checkContainsOccasional(MasterCategory master, String subcat) {
    Panel panel = getOccasionalSeriesPanel();
    assertTrue(panel.containsUIComponent(RadioButton.class, "occasionalSeries" + ":" + master.getName()));
    assertTrue(panel.containsUIComponent(RadioButton.class, "occasionalSeries" + ":" + master.getName() + ":" + subcat));
  }

  public void checkDoesNotContainOccasional(MasterCategory master, String subcat) {
    Panel panel = getOccasionalSeriesPanel();
    assertTrue(panel.containsUIComponent(RadioButton.class, "occasionalSeries" + ":" + master.getName()));
    assertFalse(panel.containsUIComponent(RadioButton.class, "occasionalSeries" + ":" + master.getName() + ":" + subcat));
  }

  public void checkDoesNotContainOccasional(MasterCategory master) {
    Panel panel = getOccasionalSeriesPanel();
    assertFalse(panel.containsUIComponent(RadioButton.class, "occasionalSeries" + ":" + master.getName()));
  }

  public void checkOccasionalSeries(MasterCategory category) {
    assertTrue(getBudgetAreasPanel().getToggleButton("occasional").isSelected());
    assertTrue(getOccasionalSeriesPanel().getRadioButton("occasionalSeries" + ":" + category.getName()).isSelected());
  }

  public void checkOccasionalContainLabel(String category) {
    assertTrue(getBudgetAreasPanel().getToggleButton("occasional").isSelected());
    assertTrue(getOccasionalSeriesPanel().getRadioButton(category).isEnabled());
  }

  public void checkContainsOccasionalCategories(String[] names) {
    selectOccasional();
    Panel panel = getOccasionalSeriesPanel();
    for (String name : names) {
      UISpecAssert.assertTrue(panel.getRadioButton(name).isVisible());
    }
  }

  public CategorizationChecker checkOccasionalSeriesIsSelected(MasterCategory category) {
    assertTrue(getBudgetAreasPanel().getToggleButton("occasional").isSelected());
    Panel panel = getOccasionalSeriesPanel();
    assertFalse(panel.getRadioButton("invisibleOccasionalToggle").isSelected());
    assertTrue(panel.getRadioButton("occasionalSeries" + ":" + category.getName()).isSelected());
    return this;
  }

  public CategorizationChecker checkEnvelopeSeriesIsSelected(String seriesName, MasterCategory category) {
    checkEnvelopeSeriesSelected(seriesName, category, true);
    return this;
  }

  public CategorizationChecker checkEnvelopeSeriesNotSelected(String seriesName, MasterCategory category) {
    checkEnvelopeSeriesSelected(seriesName, category, false);
    return this;
  }

  private CategorizationChecker checkEnvelopeSeriesSelected(String seriesName, MasterCategory category, boolean selected) {
    assertTrue(getPanel().getToggleButton("envelopes").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    assertEquals(!selected, panel.getRadioButton("invisibleSelector").isSelected());
    assertEquals(selected, panel.getRadioButton(seriesName + ":" + category.getName()).isSelected());
    return this;
  }

  public SeriesEditionDialogChecker createIncomeSeries() {
    return createSeries("income", true);
  }

  public SeriesEditionDialogChecker createRecurringSeries() {
    return createSeries("recurring", true);
  }

  public SeriesEditionDialogChecker createEnvelopeSeries() {
    return createSeries("envelope", false);
  }

  public SeriesEditionDialogChecker createSpecialSeries() {
    return createSeries("special", false);
  }

  public SeriesEditionDialogChecker createSavingsSeries() {
    return createSeries("savings", true);
  }

  private SeriesEditionDialogChecker createSeries(String type, boolean oneSelection) {
    Button button = getPanel().getPanel(type + "SeriesChooser").getButton("createSeries");
    return SeriesEditionDialogChecker.open(button, oneSelection);
  }

  public CategorizationChecker checkEditIncomeSeriesDisabled() {
    assertFalse(getIncomeSeriesPanel().getButton("editSeries").isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker editSeries(String seriesLabel, final boolean singleCategorySeries) {
    Button button = getPanel().getPanel("seriesCard").getButton("editSeries:" + seriesLabel);
    return SeriesEditionDialogChecker.open(button, singleCategorySeries);
  }

  public SeriesEditionDialogChecker editSeries(boolean isSingleSelection) {
    return SeriesEditionDialogChecker.open(getPanel().getButton("editSeries"), isSingleSelection);
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
    checkSelectedTableRows(getTable().getRowIndex(2, label));
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
      Assert.fail("Transaction " + label + "not found. Actual content:\n" + getTable().toString());
    }
    getTable().selectRow(index);
    return this;
  }

  private int getRowIndex(String label) {
    return getTable().getRowIndex(LABEL_COLUMN_INDEX, label);
  }

  public CategorizationChecker doubleClickTableRow(int row) {
    getTable().doubleClick(row, 0);
    return this;
  }

  public CategorizationChecker selectNoTableRow() {
    getTable().clearSelection();
    return this;
  }

  public CategorizationChecker selectAllTableRows() {
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

  public CategorizationChecker selectTableRow(String label) {
    return selectTableRows(label);
  }

  public CategorizationChecker selectTableRows(String... labels) {
    int lenght = 0;
    List<int[]> indices = new ArrayList<int[]>();
    for (String label : labels) {
      int[] ints = getRowIndices(label.toUpperCase());
      lenght += ints.length;
      indices.add(ints);
      if (ints.length == 0) {
        Assert.fail("Operation '" + label + "' not found");
      }
    }
    int[] ind = new int[lenght];
    int j = 0;
    for (int[] indice : indices) {
      for (int i : indice) {
        ind[j] = i;
        j++;
      }
    }
    selectTableRows(ind);
    return this;
  }

  public CategorizationChecker unselectAllTransactions() {
    getTable().clearSelection();
    return this;
  }

  public CategoryEditionChecker editOccasionalCategories() {
    selectOccasional();
    Window editionDialog = WindowInterceptor.getModalDialog(getPanel().getButton("editCategories").triggerClick());
    return new CategoryEditionChecker(editionDialog);
  }

  public Table getTable() {
    Table table = getPanel().getTable();
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
    for (int indice : indices) {
      selectTableRow(indice);
      selectIncome();
      selectExceptionalIncomeSeries(seriesName, first);
      first = false;
    }
  }

  public CategorizationChecker setIncome(String label, String seriesName, boolean createSeries) {
    int[] rows = getRowIndices(label);
    selectTableRows(rows);
    selectIncome();
    selectIncomeSeries(seriesName, createSeries);
    return this;
  }

  public CategorizationChecker setRecurring(int rowIndex, String seriesName, MasterCategory category, boolean createSeries) {
    selectTableRow(rowIndex);
    selectRecurring();
    selectRecurringSeries(seriesName, category, createSeries);
    return this;
  }

  public CategorizationChecker setRecurring(String label, String seriesName, MasterCategory category, boolean createSeries) {
    int[] rows = getRowIndices(label);
    boolean first = createSeries;
    for (int row : rows) {
      setRecurring(row, seriesName, category, first);
      first = false;
    }
    return this;
  }

  private int[] getRowIndices(String label) {
    int[] index = getTable().getRowIndices(LABEL_COLUMN_INDEX, label.toUpperCase());
    if (index.length <= 0) {
      Assert.fail("Label '" + label + "' not found");
    }
    return index;
  }

  public CategorizationChecker setEnvelope(int rowIndex, String seriesName, MasterCategory master, boolean createSeries) {
    selectTableRows(rowIndex);
    selectEnvelopes();
    selectEnvelopeSeries(seriesName, master, createSeries);
    return this;
  }

  public CategorizationChecker setEnvelope(String label, String seriesName, MasterCategory master, boolean createSeries) {
    int[] indices = getRowIndices(label);
    boolean first = createSeries;
    for (int indice : indices) {
      setEnvelope(indice, seriesName, master, first);
      first = false;
    }
    return this;
  }

  public CategorizationChecker setOccasional(String label, MasterCategory category) {
    int[] indices = getRowIndices(label);
    for (int indice : indices) {
      setOccasional(indice, category);
    }
    return this;
  }

  public CategorizationChecker setOccasional(String label, MasterCategory masterCategory, String category) {
    int[] indices = getRowIndices(label);
    for (int indice : indices) {
      setOccasional(indice, masterCategory, category);
    }
    return this;
  }

  public CategorizationChecker setOccasional(int rowIndex, MasterCategory category) {
    selectTableRow(rowIndex);
    selectOccasional();
    selectOccasionalSeries(category);
    return this;
  }

  public CategorizationChecker setOccasional(int rowIndex, MasterCategory masterCategory, String category) {
    selectTableRow(rowIndex);
    selectOccasional();
    selectOccasionalSeries(masterCategory, category);
    return this;
  }

  public CategorizationChecker setSpecial(String label, String seriesName, MasterCategory master, boolean createSeries) {
    boolean first = createSeries;
    for (int index : getRowIndices(label)) {
      setSpecial(index, seriesName, master, first);
      first = false;
    }
    return this;
  }

  public CategorizationChecker setSpecial(int rowIndex, String seriesName, MasterCategory master, boolean createSeries) {
    selectTableRows(rowIndex);
    selectSpecial();
    selectSpecialSeries(seriesName, master, createSeries);
    return this;
  }

  public CategorizationChecker checkSpecialSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(getPanel().getToggleButton("special").isSelected());
    Panel panel = getSpecialSeriesPanel();
    assertFalse(panel.getRadioButton("invisibleSelector").isSelected());
    assertTrue(panel.getRadioButton(seriesName + ":" + category.getName()).isSelected());
    return this;
  }

  public CategorizationChecker setSavings(String label, String seriesName) {
    for (int index : getRowIndices(label)) {
      setSavings(index, seriesName);
    }
    return this;
  }

  public CategorizationChecker createAndSetSavings(String label, String seriesName, String fromAccount, String toAccount) {
    boolean first = true;
    for (int index : getRowIndices(label)) {
      if (first) {
        selectTableRows(index);
        selectSavings();
        createSavingsSeries()
          .setName(seriesName)
          .setFromAccount(fromAccount)
          .setToAccount(toAccount)
          .setCategory(MasterCategory.SAVINGS)
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
    selectSavings();
    selectSavingsSeries(seriesName);
    return this;
  }

  public CategorizationChecker checkSavingsSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(getPanel().getToggleButton("savings").isSelected());
    Panel panel = getSavingsSeriesPanel();
    assertTrue(panel.getRadioButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkNoSeriesMessage(String text) {
    TextBox textBox = getPanel().getTextBox("noSeriesMessage");
    assertTrue(textBox.isVisible());
    assertThat(textBox.textContains(text));
    return this;
  }

  public CategorizationChecker checkNoSeriesMessageHidden() {
    assertFalse(getPanel().getTextBox("noSeriesMessage").isVisible());
    return this;
  }

  public CategorizationGaugeChecker getGauge() {
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

  public void checkShowsUncategorizedTransactionsOnly() {
    checkTransctionFilterMode(TransactionFilteringMode.UNCATEGORIZED);
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

  public ConfirmationDialogChecker delete(int row) {
    getTable().selectRow(row);
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

    public CategorizationTableChecker add(String date, TransactionType prelevement, String label, String note, double amount, MasterCategory category) {
      if (!label.startsWith("Planned")) {
        label = label.toUpperCase();
      }
      add(new Object[]{date, getCategoryName(category), label, amount});
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