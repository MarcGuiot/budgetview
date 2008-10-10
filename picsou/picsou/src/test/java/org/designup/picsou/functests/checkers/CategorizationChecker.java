package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.gui.categorization.components.TransactionFilteringMode;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.AbstractButton;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategorizationChecker extends DataChecker {
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
    ToggleButton invisibleToggle = (ToggleButton)toggles[0];
    assertFalse(invisibleToggle.isVisible());
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
        .switchToManual()
        .selectAllMonths()
        .setAmount("0")
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
      panel.getToggleButton(name).click();
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
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    for (UIComponent toggle : toggles) {
      if (toggle.getAwtComponent().isVisible()) {
        assertFalse(toggle.getLabel() + " selected", ((ToggleButton)toggle).isSelected());
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
      panel.getToggleButton(name).click();
    }
    return this;
  }

  public CategorizationChecker categorizeInRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
    panel.getToggleButton(name).click();
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
      panel.getToggleButton(name).click();
    }
    return this;
  }

  public CategorizationChecker selectRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
    panel.getToggleButton(name).click();
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
    assertTrue(panel.getToggleButton(label).isEnabled());
    return this;
  }

  public CategorizationChecker checkRecurringSeriesIsSelected(String seriesName) {
    assertTrue(getPanel().getToggleButton("Recurring").isSelected());

    Panel panel = getRecurringSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkRecurringSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(getPanel().getPanel("recurringSeriesChooser").getToggleButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkIncomeSeriesIsSelected(String seriesName) {
    assertTrue(getPanel().getToggleButton("Income").isSelected());

    Panel panel = getIncomeSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());
    return this;
  }

  public CategorizationChecker checkIncomeSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(getPanel().getPanel("incomeSeriesChooser").getToggleButton(seriesName).isSelected());
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
      assertTrue(panel.getToggleButton(label).isEnabled());
    }
    return this;
  }

  public CategorizationChecker checkContainsEnvelope(String envelopeName, MasterCategory... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (MasterCategory category : categories) {
      panel.getToggleButton(envelopeName + ":" + category.getName());
      assertTrue(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category.getName()));
    }
    return this;
  }

  public CategorizationChecker checkContainsEnvelope(String envelopeName, String... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (String category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category));
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
      assertFalse(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category));
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
      String toggleName = envelopeName + ":" + category.getName();
      panel.getToggleButton(toggleName).click();
    }
    return this;
  }

  public CategorizationChecker selectEnvelopeSeries(String envelopeName, String category) {
    Panel panel = getEnvelopeSeriesPanel();
    panel.getToggleButton(envelopeName + ":" + category).click();
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
    Component component = panel.findSwingComponent(ComponentMatchers.innerNameIdentity(name));
    if (component != null) {
      // TODO avec la multi affectation de category a une enveloppe
    }
    if (createSeries) {
      createSpecialSeries()
        .setName(seriesName)
        .setCategory(category)
        .validate();
    }
    else {
      panel.getToggleButton(name).click();
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

  public CategorizationChecker selectSavingsSeries(String savingsName, MasterCategory category, boolean createSeries) {
    Panel panel = getSavingsSeriesPanel();
    if (createSeries) {
      createSavingsSeries()
        .setName(savingsName)
        .setCategory(category)
        .validate();
    }
    else {
      panel.getToggleButton(savingsName).click();
    }
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
    getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).click();
    return this;
  }

  public CategorizationChecker selectOccasionalSeries(MasterCategory masterCategory, String subcat) {
    selectOccasional();
    final String toggleName = "occasionalSeries" + ":" + masterCategory.getName() + ":" + subcat;
    getOccasionalSeriesPanel().getToggleButton(toggleName).click();
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
      assertTrue(panel.containsUIComponent(ToggleButton.class, "occasionalSeries" + ":" + category.getName()));
    }
  }

  public void checkContainsOccasional(MasterCategory master, String subcat) {
    Panel panel = getOccasionalSeriesPanel();
    assertTrue(panel.containsUIComponent(ToggleButton.class, "occasionalSeries" + ":" + master.getName()));
    assertTrue(panel.containsUIComponent(ToggleButton.class, "occasionalSeries" + ":" + master.getName() + ":" + subcat));
  }

  public void checkDoesNotContainOccasional(MasterCategory master, String subcat) {
    Panel panel = getOccasionalSeriesPanel();
    assertTrue(panel.containsUIComponent(ToggleButton.class, "occasionalSeries" + ":" + master.getName()));
    assertFalse(panel.containsUIComponent(ToggleButton.class, "occasionalSeries" + ":" + master.getName() + ":" + subcat));
  }

  public void checkDoesNotContainOccasional(MasterCategory master) {
    Panel panel = getOccasionalSeriesPanel();
    assertFalse(panel.containsUIComponent(ToggleButton.class, "occasionalSeries" + ":" + master.getName()));
  }

  public void checkOccasionalSeries(MasterCategory category) {
    assertTrue(getBudgetAreasPanel().getToggleButton("occasional").isSelected());
    assertTrue(getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).isSelected());
  }

  public void checkOccasionalContainLabel(String category) {
    assertTrue(getBudgetAreasPanel().getToggleButton("occasional").isSelected());
    assertTrue(getOccasionalSeriesPanel().getToggleButton(category).isEnabled());
  }

  public void checkContainsOccasionalCategories(String[] names) {
    selectOccasional();
    Panel panel = getOccasionalSeriesPanel();
    for (String name : names) {
      UISpecAssert.assertTrue(panel.getToggleButton(name).isVisible());
    }
  }

  public CategorizationChecker checkOccasionalSeriesIsSelected(MasterCategory category) {
    assertTrue(getBudgetAreasPanel().getToggleButton("occasional").isSelected());
    Panel panel = getOccasionalSeriesPanel();
    assertFalse(panel.getToggleButton("invisibleOccasionalToggle").isSelected());
    assertTrue(panel.getToggleButton("occasionalSeries" + ":" + category.getName()).isSelected());
    return this;
  }

  public CategorizationChecker checkEnvelopeSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(getPanel().getToggleButton("envelopes").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    assertFalse(panel.getToggleButton("invisibleToggle").isSelected());
    assertTrue(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
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

  public SeriesEditionDialogChecker createSeries(String type, boolean oneSelection) {
    Button button = getPanel().getPanel(type + "SeriesChooser").getButton("createSeries");
    final Window creationDialog = WindowInterceptor.getModalDialog(button.triggerClick());
    return new SeriesEditionDialogChecker(creationDialog, oneSelection);
  }

  public CategorizationChecker checkEditIncomeSeriesDisabled() {
    assertFalse(getIncomeSeriesPanel().getButton("editSeries").isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker editSeries(boolean isSingleSelection) {
    final Window creationDialog = WindowInterceptor.getModalDialog(getPanel().getButton("editSeries").triggerClick());
    return new SeriesEditionDialogChecker(creationDialog, isSingleSelection);
  }

  public CategorizationChecker checkTable(Object[][] content) {
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

  private int getRowIndex(String label) {
    return getTable().getRowIndex(LABEL_COLUMN_INDEX, label);
  }

  public CategorizationChecker doubleClickTableRow(int row) {
    getTable().doubleClick(row, 0);
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

  public CategorizationChecker selectTableRows(String... labels) {
    int lenght = 0;
    List<int[]> indices = new ArrayList<int[]>();
    for (String label : labels) {
      int[] ints = getRowIndices(label);
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

  /**
   * @deprecated
   */
  public void checkAutoSelectSimilarEnabled(boolean enabled) {
    assertEquals(enabled, getPanel().getCheckBox("similar").isSelected());
  }

  /**
   * @deprecated
   */
  public void enableAutoSelectSimilar() {
    getPanel().getCheckBox("similar").select();
  }

  /**
   * @deprecated
   */
  public void disableAutoSelectSimilar() {
    getPanel().getCheckBox("similar").unselect();
  }

  public void checkAutoHideEnabled(boolean enabled) {
    assertEquals(enabled, getPanel().getCheckBox("hide").isSelected());
  }

  public void enableAutoHide() {
    getPanel().getCheckBox("hide").select();
  }

  public CategorizationChecker disableAutoHide() {
    getPanel().getCheckBox("hide").unselect();
    return this;
  }

  public void checkAutoSelectNextEnabled(boolean enabled) {
    assertEquals(enabled, getPanel().getCheckBox("next").isSelected());
  }

  public void enableAutoSelectNext() {
    getPanel().getCheckBox("next").select();
  }

  public CategorizationChecker disableAutoSelectNext() {
    getPanel().getCheckBox("next").unselect();
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
    int[] index = getTable().getRowIndices(LABEL_COLUMN_INDEX, label);
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
    int[] indices = getRowIndices(label);
    boolean first = createSeries;
    for (int indice : indices) {
      setSpecial(indice, seriesName, master, first);
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
    assertFalse(panel.getToggleButton("invisibleToggle").isSelected());
    assertTrue(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
    return this;
  }

  public CategorizationChecker setSavings(String label, String seriesName, MasterCategory master, boolean createSeries) {
    int[] indices = getRowIndices(label);
    boolean first = createSeries;
    for (int indice : indices) {
      setSavings(indice, seriesName, master, first);
      first = false;
    }
    return this;
  }

  public CategorizationChecker setSavings(int rowIndex, String seriesName, MasterCategory master, boolean createSeries) {
    int[] rows = new int[]{rowIndex};
    selectTableRows(rows);
    selectSavings();
    selectSavingsSeries(seriesName, master, createSeries);
    return this;
  }

  public CategorizationChecker checkSavingsSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(getPanel().getToggleButton("savings").isSelected());
    Panel panel = getSavingsSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());
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
    assertThat(getPanel().getComboBox("transactionFilterCombo").selectionEquals(TransactionFilteringMode.ALL.toString()));
  }

  public void showAllTransactions() {
    selectTransactionFilterMode(TransactionFilteringMode.ALL.toString());
  }

  public void showSelectedMonthsOnly() {
    selectTransactionFilterMode(TransactionFilteringMode.SELECTED_MONTHS.toString());
  }

  private void selectTransactionFilterMode(String mode) {
    getPanel().getComboBox("transactionFilterCombo").select(mode);
  }

  public void showLastImportedFileOnly() {
    selectTransactionFilterMode(TransactionFilteringMode.LAST_IMPORTED_FILE.toString());
  }

  public class CategorizationTableChecker extends TableChecker {

    private CategorizationTableChecker() {
    }

    public CategorizationTableChecker add(String date, TransactionType prelevement, String label, String note, double amount, String series) {
      add(new Object[]{date, series, label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, TransactionType prelevement, String label, String note, double amount, MasterCategory category) {
      add(new Object[]{date, getCategoryName(category), label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, TransactionType prelevement, String label, String note, double amount) {
      add(new Object[]{date, "", label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, String label, double amount) {
      add(new Object[]{date, "", label, amount});
      return this;
    }

    public CategorizationTableChecker add(String date, String series, String label, double amount) {
      add(new Object[]{date, series, label, amount});
      return this;
    }

    protected Table getTable() {
      return CategorizationChecker.this.getTable();
    }
  }
}