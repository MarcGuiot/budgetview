package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategorizationDialogChecker extends DataChecker {
  private Window mainWindow;
  private static final int LABEL_COLUMN_INDEX = 1;

  public CategorizationDialogChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  private Panel getPanel() {
    return mainWindow.getPanel("categorizationView");
  }

  public CategorizationDialogChecker checkLabel(String expected) {
    assertTrue(getTransactionLabel().textEquals(expected));
    return this;
  }

  public CategorizationDialogChecker checkLabel(int count) {
    assertTrue(getTransactionLabel().textEquals(count + " operations are selected."));
    return this;
  }

  public void checkBudgetAreasAreEnabled() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area != BudgetArea.UNCATEGORIZED) {
        assertTrue(getPanel().getToggleButton(area.getName()).isEnabled());
      }
    }
  }

  public void checkBudgetAreasAreDisabled() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area != BudgetArea.UNCATEGORIZED) {
        assertFalse(getPanel().getToggleButton(area.getName()).isEnabled());
      }
    }
  }

  public void checkBudgetAreaIsSelected(BudgetArea budgetArea) {
    assertTrue(getPanel().getToggleButton(budgetArea.getGlob().get(BudgetArea.NAME)).isSelected());
  }

  public void checkNoBudgetAreaSelected() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area != BudgetArea.UNCATEGORIZED) {
        final String name = area.getGlob().get(BudgetArea.NAME);
        assertFalse("Area '" + name + "' is selected", getPanel().getToggleButton(name).isSelected());
      }
    }
    assertTrue(getPanel().getTextBox("Select the series type").isVisible());
  }

  public CategorizationDialogChecker selectIncome() {
    getPanel().getPanel("budgetAreas").getToggleButton(BudgetArea.INCOME.getGlob().get(BudgetArea.NAME)).click();
    return this;
  }

  public CategorizationDialogChecker checkNoIncomeSeriesDisplayed() {
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

  public CategorizationDialogChecker selectExceptionalIncomeSeries(String name, boolean showSeriesInitialization) {
    Panel panel = getIncomeSeriesPanel();
    if (showSeriesInitialization) {
      createIncomeSeries()
        .setName(name)
        .setCategory(MasterCategory.INCOME)
        .selectAllMonths()
        .setAmount("0")
        .validate();
      return this;
    }
    panel.getToggleButton(name).click();
    return this;
  }

  public CategorizationDialogChecker selectIncomeSeries(String name, boolean showSeriesInitialization) {
    Panel panel = getIncomeSeriesPanel();
    if (showSeriesInitialization) {
      createIncomeSeries()
        .setName(name)
        .setCategory(MasterCategory.INCOME)
        .validate();
      return this;
    }
    panel.getToggleButton(name).click();
    return this;
  }

  private Panel getIncomeSeriesPanel() {
    Panel panel = this.getPanel().getPanel("incomeSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationDialogChecker selectRecurring() {
    getPanel().getToggleButton(BudgetArea.RECURRING_EXPENSES.getGlob().get(BudgetArea.NAME)).click();
    return this;
  }

  public CategorizationDialogChecker checkContainsRecurringSeries(String... seriesNames) {
    Panel seriesPanel = getRecurringSeriesPanel();

    List<String> names = new ArrayList<String>();
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    for (UIComponent toggle : toggles) {
      names.add(toggle.getLabel());
    }

    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
    return this;
  }

  public CategorizationDialogChecker checkRecurringSeriesNotFound(String seriesName) {
    Panel seriesPanel = getRecurringSeriesPanel();
    assertFalse(seriesPanel.containsUIComponent(ToggleButton.class, seriesName));
    return this;
  }

  public CategorizationDialogChecker selectNewRecurringSeries(String name, MasterCategory category,
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

  public CategorizationDialogChecker selectRecurringSeries(String name, MasterCategory category, boolean createSeries) {
    Panel panel = getRecurringSeriesPanel();
    if (createSeries) {
      createRecurringSeries()
        .setName(name)
        .setCategory(category)
        .validate();
      return this;
    }
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

  public CategorizationDialogChecker checkContainsButtonInReccuring(String label) {
    Panel panel = getRecurringSeriesPanel();
    assertTrue(panel.getToggleButton(label).isEnabled());
    return this;
  }

  public CategorizationDialogChecker checkRecurringSeriesIsSelected(String seriesName) {
    assertTrue(getPanel().getToggleButton("RecurringExpenses").isSelected());

    Panel panel = getRecurringSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());
    return this;
  }

  public CategorizationDialogChecker checkRecurringSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(getPanel().getPanel("recurringSeriesChooser").getToggleButton(seriesName).isSelected());
    return this;
  }

  public CategorizationDialogChecker checkIncomeSeriesIsSelected(String seriesName) {
    assertTrue(getPanel().getToggleButton("Income").isSelected());

    Panel panel = getIncomeSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());

    return this;
  }

  public CategorizationDialogChecker checkIncomeSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(getPanel().getPanel("incomeSeriesChooser").getToggleButton(seriesName).isSelected());
    return this;
  }

  public CategorizationDialogChecker selectEnvelopes() {
    getPanel().getToggleButton("expensesEnvelope").click();
    return this;
  }

  public CategorizationDialogChecker checkContainsLabelInEnvelope(String label) {
    Panel panel = getEnvelopeSeriesPanel();
    Assertion assertion = panel.containsLabel(label);
    if (!assertion.isTrue()) {
      assertTrue(panel.getToggleButton(label).isEnabled());
    }
    return this;
  }

  public CategorizationDialogChecker checkContainsEnvelope(String envelopeName, MasterCategory... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (MasterCategory category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category.getName()));
    }
    return this;
  }

  public CategorizationDialogChecker checkContainsEnvelope(String envelopeName, String... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (String category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category));
    }
    return this;
  }

  public CategorizationDialogChecker selectEnvelopeSeries(String envelopeName, MasterCategory category, boolean createSeries) {
    Panel panel = getEnvelopeSeriesPanel();
    String name = envelopeName + ":" + category.getName();
    if (createSeries) {
      createEnvelopeSeries()
        .setName(envelopeName)
        .setCategory(category)
        .validate();
      return this;
    }
    panel.getToggleButton(name).click();
    return this;
  }

  public CategorizationDialogChecker selectEnvelopeSeries(String envelopeName, String category) {
    Panel panel = getEnvelopeSeriesPanel();
    String name = envelopeName + ":" + category;
    panel.getToggleButton(name).click();
    return this;
  }


  private Panel getEnvelopeSeriesPanel() {
    Panel panel = this.getPanel().getPanel("envelopeSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationDialogChecker selectProjects() {
    getPanel().getToggleButton("projects").click();
    return this;
  }

  public void checkContainsProject(String projectName, MasterCategory... categories) {
    Panel panel = getProjectSeriesPanel();
    assertTrue(panel.containsLabel(projectName));
    for (MasterCategory category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, projectName + ":" + category.getName()));
    }
  }

  public CategorizationDialogChecker selectProjectSeries(String projectName, MasterCategory category, boolean createSeries) {
    Panel panel = getProjectSeriesPanel();
    String name = projectName + ":" + category.getName();
    Component component = panel.findSwingComponent(ComponentMatchers.innerNameIdentity(name));
    if (component != null) {
      // TODO avec la multi affectation de category a une enveloppe
    }
    if (createSeries) {
      createProjectSeries()
        .setName(projectName)
        .setCategory(category)
        .validate();
      return this;
    }
    panel.getToggleButton(name).click();
    return this;
  }

  private Panel getProjectSeriesPanel() {
    Panel panel = this.getPanel().getPanel("projectSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationDialogChecker selectSavings() {
    getPanel().getToggleButton("savings").click();
    return this;
  }

  public void checkContainsSavings(String savingsName, MasterCategory... categories) {
    Panel panel = getSavingsSeriesPanel();
    assertTrue(panel.containsLabel(savingsName));
    for (MasterCategory category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, savingsName + ":" + category.getName()));
    }
  }

  public CategorizationDialogChecker selectSavingsSeries(String savingsName, MasterCategory category, boolean createSeries) {
    Panel panel = getSavingsSeriesPanel();
    String name = savingsName + ":" + category.getName();
    Component component = panel.findSwingComponent(ComponentMatchers.innerNameIdentity(name));
    if (component != null) {
      // TODO avec la multi affectation de category a une enveloppe
    }
    if (createSeries) {
      createSavingsSeries()
        .setName(savingsName)
        .setCategory(category)
        .validate();
      return this;
    }
    panel.getToggleButton(name).click();
    return this;
  }

  private Panel getSavingsSeriesPanel() {
    Panel panel = this.getPanel().getPanel("savingsSeriesChooser");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationDialogChecker selectOccasional() {
    getPanel().getToggleButton("occasionalExpenses").click();
    return this;
  }

  public CategorizationDialogChecker selectOccasionalSeries(MasterCategory category) {
    selectOccasional();
    getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).click();
    return this;
  }

  public CategorizationDialogChecker selectOccasionalSeries(MasterCategory masterCategory, String subcat) {
    selectOccasional();
    final String toggleName = "occasionalSeries" + ":" + masterCategory.getName() + ":" + subcat;
    getOccasionalSeriesPanel().getToggleButton(toggleName).click();
    return this;
  }

  private Panel getOccasionalSeriesPanel() {
    Panel panel = this.getPanel().getPanel("occasionalSeriesChooser");
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

  public void checkOccasionalSeries(MasterCategory category) {
    assertTrue(getPanel().getToggleButton("occasionalExpenses").isSelected());
    assertTrue(getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).isSelected());
  }

  public void checkOccasionalContainLabel(String category) {
    assertTrue(getPanel().getToggleButton("occasionalExpenses").isSelected());
    assertTrue(getOccasionalSeriesPanel().getToggleButton(category).isEnabled());
  }

  public void checkContainsOccasionalCategories(String[] names) {
    selectOccasional();
    Panel panel = getOccasionalSeriesPanel();
    for (String name : names) {
      UISpecAssert.assertTrue(panel.getToggleButton(name).isVisible());
    }
  }

  public void checkEnvelopeSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(getPanel().getToggleButton("expensesEnvelope").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
  }

  public void checkEnveloppeSeriesIsNotSelected(String seriesName, MasterCategory category) {
    assertTrue(getPanel().getToggleButton("expensesEnvelope").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    UISpecAssert.assertFalse(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
  }

  public void checkNextIsEnabled() {
    UISpecAssert.assertTrue(getPanel().getButton("nextTransaction").isEnabled());
  }

  public void checkNextIsDisabled() {
    UISpecAssert.assertFalse(getPanel().getButton("nextTransaction").isEnabled());
  }

  public void selectNext() {
    getPanel().getButton("nextTransaction").click();
  }

  public void assertVisible(boolean visible) {
    assertEquals(visible, getPanel().isVisible());
  }

  public void pressEscapeKey() {
    final JDialog jDialog = (JDialog)getPanel().getAwtComponent();
    KeyUtils.pressKey(jDialog.getRootPane(), Key.ESCAPE);
  }

  public void checkTextVisible(String text) {
    Assert.assertNotNull(getPanel().getTextBox(text));
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

  public SeriesEditionDialogChecker createProjectSeries() {
    return createSeries("project", false);
  }

  public SeriesEditionDialogChecker createSavingsSeries() {
    return createSeries("savings", true);
  }

  public SeriesEditionDialogChecker createSeries(String type, boolean oneSelection) {
    Button button = getPanel().getPanel(type + "SeriesChooser").getButton("createSeries");
    final Window creationDialog = WindowInterceptor.getModalDialog(button.triggerClick());
    return new SeriesEditionDialogChecker(creationDialog, oneSelection);
  }

  public CategorizationDialogChecker checkEditIncomeSeriesDisabled() {
    assertFalse(getIncomeSeriesPanel().getButton("editSeries").isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker editSeries(boolean isSingleSelection) {
    final Window creationDialog = WindowInterceptor.getModalDialog(getPanel().getButton("editSeries").triggerClick());
    return new SeriesEditionDialogChecker(creationDialog, isSingleSelection);
  }

  public CategorizationDialogChecker checkTable(Object[][] content) {
    assertTrue(getTable().contentEquals(content));
    return this;
  }

  public CategorizationDialogChecker checkTableIsEmpty() {
    assertTrue(getTable().isEmpty());
    return this;
  }

  public CategorizationDialogChecker checkSelectedTableRows(int... rows) {
    assertTrue(getTable().rowsAreSelected(rows));
    return this;
  }

  public CategorizationDialogChecker checkNoTransactionSelected() {
    assertTrue(getTable().selectionIsEmpty());
    return this;
  }

  public CategorizationDialogChecker selectTableRow(int row) {
    selectTableRows(row);
    return this;
  }

  public CategorizationDialogChecker selectTableRows(int... rows) {
    getTable().selectRows(rows);
    return this;
  }

  public CategorizationDialogChecker selectTableRows(String... labels) {
    int rows[] = new int[labels.length];
    for (int i = 0; i < labels.length; i++) {
      rows[i] = getTable().getRowIndex(1, labels[i]);
    }
    selectTableRows(rows);
    return this;
  }

  public CategorizationDialogChecker unselectAllTransactions() {
    getTable().clearSelection();
    return this;
  }

  public void checkAutoSelectSimilarEnabled(boolean enabled) {
    assertEquals(enabled, getPanel().getCheckBox("similar").isSelected());
  }

  public void enableAutoSelection() {
    getPanel().getCheckBox("similar").select();
  }

  public void disableAutoSelection() {
    getPanel().getCheckBox("similar").unselect();
  }

  public void checkAutoHideEnabled(boolean enabled) {
    assertEquals(enabled, getPanel().getCheckBox("hide").isSelected());
  }

  public void enableAutoHide() {
    getPanel().getCheckBox("hide").select();
  }

  public CategorizationDialogChecker disableAutoHide() {
    getPanel().getCheckBox("hide").unselect();
    return this;
  }

  public void checkAutoSelectNextEnabled(boolean enabled) {
    assertEquals(enabled, getPanel().getCheckBox("next").isSelected());
  }

  public void enableAutoSelectNext() {
    getPanel().getCheckBox("next").select();
  }

  public CategorizationDialogChecker disableAutoSelectNext() {
    getPanel().getCheckBox("next").unselect();
    return this;
  }

  public void checkClosed() {
    assertFalse(getPanel().isVisible());
  }

  public CategoryEditionChecker editOccasionalCategories() {
    selectOccasional();
    Window editionDialog = WindowInterceptor.getModalDialog(getPanel().getButton("editCategories").triggerClick());
    return new CategoryEditionChecker(editionDialog);
  }

  public Table getTable() {
    Table table = getPanel().getTable();
    table.setCellValueConverter(0, new DateCellConverter());
    table.setCellValueConverter(2, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob)modelObject;
        return transaction.get(Transaction.AMOUNT);
      }
    });
    return table;
  }

  private TextBox getTransactionLabel() {
    return getPanel().getTextBox("transactionLabel");
  }

  public void setExceptionalIncome(String label, String seriesName, boolean showSeriesInitialization) {
    selectTableRow(getRowIndex(label));
    selectIncome();
    selectExceptionalIncomeSeries(seriesName, showSeriesInitialization);
  }

  public CategorizationDialogChecker setIncome(String label, String seriesName, boolean showSeriesInitialization) {
    selectTableRows(getRowIndex(label));
    selectIncome();
    selectIncomeSeries(seriesName, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setRecurring(int rowIndex, String seriesName, MasterCategory category, boolean showSeriesInitialization) {
    selectTableRow(rowIndex);
    selectRecurring();
    selectRecurringSeries(seriesName, category, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setRecurring(String label, String seriesName, MasterCategory category, boolean showSeriesInitialization) {
    setRecurring(getRowIndex(label), seriesName, category, showSeriesInitialization);
    return this;
  }

  private int getRowIndex(String label) {
    return getTable().getRowIndex(LABEL_COLUMN_INDEX, label);
  }

  public CategorizationDialogChecker setEnvelope(int rowIndex, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    selectTableRows(rowIndex);
    selectEnvelopes();
    selectEnvelopeSeries(seriesName, master, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setEnvelope(String label, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    setEnvelope(getRowIndex(label), seriesName, master, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setOccasional(String label, MasterCategory category) {
    setOccasional(getRowIndex(label), category);
    return this;
  }

  public CategorizationDialogChecker setOccasional(int rowIndex, MasterCategory category) {
    selectTableRow(rowIndex);
    selectOccasional();
    selectOccasionalSeries(category);
    return this;
  }

  public CategorizationDialogChecker setProject(String label, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    setProject(getRowIndex(label), seriesName, master, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setProject(int rowIndex, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    int[] rows = new int[]{rowIndex};
    selectTableRows(rows);
    selectProjects();
    selectProjectSeries(seriesName, master, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setSavings(String label, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    setSavings(getRowIndex(label), seriesName, master, showSeriesInitialization);
    return this;
  }

  public CategorizationDialogChecker setSavings(int rowIndex, String seriesName, MasterCategory master, boolean showSeriesInitialization) {
    int[] rows = new int[]{rowIndex};
    selectTableRows(rows);
    selectSavings();
    selectSavingsSeries(seriesName, master, showSeriesInitialization);
    return this;
  }
}