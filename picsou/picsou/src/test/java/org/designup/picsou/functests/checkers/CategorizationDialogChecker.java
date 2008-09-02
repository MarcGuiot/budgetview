package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
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
  private Window dialog;
  private TextBox transactionLabel;
  private Table table;

  public CategorizationDialogChecker(Window dialog) {
    this.dialog = dialog;
    this.transactionLabel = dialog.getTextBox("transactionLabel");
    table = this.dialog.getTable();
    table.setCellValueConverter(0, new DateCellConverter());
    table.setCellValueConverter(2, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Glob transaction = (Glob)modelObject;
        return transaction.get(Transaction.AMOUNT);
      }
    });
  }

  public void checkLabel(String expected) {
    assertTrue(transactionLabel.textEquals(expected));
  }

  public void checkLabel(int count) {
    assertTrue(transactionLabel.textEquals(count + " operations are selected."));
  }

  public void checkBudgetAreasAreEnabled() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area != BudgetArea.UNCATEGORIZED) {
        assertTrue(dialog.getToggleButton(area.getName()).isEnabled());
      }
    }
  }

  public void checkBudgetAreasAreDisabled() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area != BudgetArea.UNCATEGORIZED) {
        assertFalse(dialog.getToggleButton(area.getName()).isEnabled());
      }
    }
  }

  public void checkBudgetAreaIsSelected(BudgetArea budgetArea) {
    assertTrue(dialog.getToggleButton(budgetArea.getGlob().get(BudgetArea.NAME)).isSelected());
  }

  public void checkNoBudgetAreaSelected() {
    for (BudgetArea area : BudgetArea.values()) {
      if (area != BudgetArea.UNCATEGORIZED) {
        final String name = area.getGlob().get(BudgetArea.NAME);
        assertFalse("Area '" + name + "' is selected", dialog.getToggleButton(name).isSelected());
      }
    }
    assertTrue(dialog.getTextBox("Select the series type").isVisible());
  }

  public CategorizationDialogChecker selectIncome() {
    dialog.getPanel("budgetAreas").getToggleButton(BudgetArea.INCOME.getGlob().get(BudgetArea.NAME)).click();
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
      SeriesEditionDialogChecker createSerie = createSeries();
      createSerie.setName(name);
      createSerie.setCategory(MasterCategory.INCOME);
      createSerie.selectAllMonths();
      createSerie.setAmount("0");
      createSerie.validate();
    }
    panel.getToggleButton(name).click();
    return this;
  }

  public CategorizationDialogChecker selectIncomeSeries(String name, boolean showSeriesInitialization) {
    Panel panel = getIncomeSeriesPanel();
    if (showSeriesInitialization) {
      SeriesEditionDialogChecker createSerie = createSeries();
      createSerie.setName(name);
      createSerie.setCategory(MasterCategory.INCOME);
      createSerie.validate();
    }
    panel.getToggleButton(name).click();
    return this;
  }

  private Panel getIncomeSeriesPanel() {
    Panel panel = dialog.getPanel("incomeSeriesRepeat");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void selectRecurring() {
    dialog.getToggleButton(BudgetArea.RECURRING_EXPENSES.getGlob().get(BudgetArea.NAME)).click();
  }

  public void checkContainsRecurringSeries(String... seriesNames) {
    Panel seriesPanel = getRecurringSeriesPanel();

    List<String> names = new ArrayList<String>();
    UIComponent[] toggles = seriesPanel.getUIComponents(ToggleButton.class);
    for (UIComponent toggle : toggles) {
      names.add(toggle.getLabel());
    }

    org.globsframework.utils.TestUtils.assertContains(names, seriesNames);
  }

  public void checkRecurringSeriesNotFound(String seriesName) {
    Panel seriesPanel = getRecurringSeriesPanel();
    assertFalse(seriesPanel.containsUIComponent(ToggleButton.class, seriesName));
  }

  public void selectRecurringSeries(String name, MasterCategory category, boolean showSeriesInitialization) {
    Panel panel = getRecurringSeriesPanel();
    if (showSeriesInitialization) {
      SeriesEditionDialogChecker createSerie = createSeries();
      createSerie.setName(name);
      createSerie.setCategory(category);
      createSerie.validate();
    }
    panel.getToggleButton(name).click();
  }

  private Panel getRecurringSeriesPanel() {
    Panel panel = dialog.getPanel("recurringSeriesRepeat");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void checkRecurringSeriesIsSelected(String seriesName) {
    assertTrue(dialog.getToggleButton("RecurringExpenses").isSelected());

    Panel panel = getRecurringSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());
  }

  public void checkRecurringSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(dialog.getPanel("recurringSeriesRepeat").getToggleButton(seriesName).isSelected());
  }

  public void checkIncomeSeriesIsSelected(String seriesName) {
    assertTrue(dialog.getToggleButton("Income").isSelected());

    Panel panel = getIncomeSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName).isSelected());
  }

  public void checkIncomeSeriesIsNotSelected(String seriesName) {
    UISpecAssert.assertFalse(dialog.getPanel("incomeSeriesRepeat").getToggleButton(seriesName).isSelected());
  }

  public void selectEnvelopes() {
    dialog.getToggleButton("expensesEnvelope").click();
  }

  public void checkContainsEnvelope(String envelopeName, MasterCategory... categories) {
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.containsLabel(envelopeName));
    for (MasterCategory category : categories) {
      assertTrue(panel.containsUIComponent(ToggleButton.class, envelopeName + ":" + category.getName()));
    }
  }

  public void selectEnvelopeSeries(String envelopeName, MasterCategory category, boolean showSeriesInitialization) {
    Panel panel = getEnvelopeSeriesPanel();
    String name = envelopeName + ":" + category.getName();
    Component component = panel.findSwingComponent(ComponentMatchers.innerNameIdentity(name));
    if (component != null) {
      // TODO avec la multi affectation de category a une enveloppe
    }
    if (showSeriesInitialization) {
      SeriesEditionDialogChecker createSerie = createSeries();
      createSerie.setName(envelopeName);
      createSerie.setCategory(category);
      createSerie.validate();
    }
    panel.getToggleButton(name).click();
  }

  private Panel getEnvelopeSeriesPanel() {
    Panel panel = dialog.getPanel("envelopeSeriesRepeat");
    assertTrue(panel.isVisible());
    return panel;
  }

  public CategorizationDialogChecker selectOccasional() {
    dialog.getToggleButton("occasionalExpenses").click();
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
    Panel panel = dialog.getPanel("occasionalSeriesRepeat");
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

  public void checkOccasionalSeries(MasterCategory category) {
    assertTrue(dialog.getToggleButton("occasionalExpenses").isSelected());
    assertTrue(getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).isSelected());
  }

  public void checkContainsOccasionalCategories(String[] names) {
    selectOccasional();
    Panel panel = getOccasionalSeriesPanel();
    for (String name : names) {
      UISpecAssert.assertTrue(panel.getToggleButton(name).isVisible());
    }
  }

  public void checkEnvelopeSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(dialog.getToggleButton("expensesEnvelope").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
  }

  public void checkEnveloppeSeriesIsNotSelected(String seriesName, MasterCategory category) {
    assertTrue(dialog.getToggleButton("expensesEnvelope").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    UISpecAssert.assertFalse(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
  }

  public void checkNextIsEnabled() {
    UISpecAssert.assertTrue(dialog.getButton("nextTransaction").isEnabled());
  }

  public void checkNextIsDisabled() {
    UISpecAssert.assertFalse(dialog.getButton("nextTransaction").isEnabled());
  }

  public void selectNext() {
    dialog.getButton("nextTransaction").click();
  }

  public void assertVisible(boolean visible) {
    assertEquals(visible, dialog.isVisible());
  }

  public void pressEscapeKey() {
    final JDialog jDialog = (JDialog)dialog.getAwtComponent();
    KeyUtils.pressKey(jDialog.getRootPane(), Key.ESCAPE);
  }

  public void validate() {
    dialog.getButton("ok").click();
  }

  public void cancel() {
    dialog.getButton("cancel").click();
  }

  public void checkTextVisible(String text) {
    Assert.assertNotNull(dialog.getTextBox(text));
  }

  public SeriesEditionDialogChecker createSeries() {
    final Window creationDialog = WindowInterceptor.getModalDialog(dialog.getButton("New series").triggerClick());
    return new SeriesEditionDialogChecker(creationDialog);
  }

  public CategorizationDialogChecker checkTable(Object[][] content) {
    assertTrue(table.contentEquals(content));
    return this;
  }

  public CategorizationDialogChecker checkTableIsEmpty() {
    assertTrue(table.isEmpty());
    return this;
  }

  public CategorizationDialogChecker checkSelectedTableRows(int... rows) {
    assertTrue(table.rowsAreSelected(rows));
    return this;
  }

  public CategorizationDialogChecker checkNoTransactionSelected() {
    assertTrue(table.selectionIsEmpty());
    return this;
  }

  public CategorizationDialogChecker selectTableRow(int row) {
    selectTableRows(row);
    return this;
  }

  public CategorizationDialogChecker selectTableRows(int... rows) {
    table.selectRows(rows);
    return this;
  }

  public CategorizationDialogChecker selectTableRows(String... labels) {
    int rows[] = new int[labels.length];
    for (int i = 0; i < labels.length; i++) {
      rows[i] = table.getRowIndex(1, labels[i]);
    }
    selectTableRows(rows);
    return this;
  }

  public CategorizationDialogChecker unselectAllTransactions() {
    table.clearSelection();
    return this;
  }

  public void checkAutoSelectionEnabled(boolean enabled) {
    assertEquals(enabled, dialog.getCheckBox("similar").isSelected());
  }

  public void enableAutoSelection() {
    dialog.getCheckBox("similar").select();
  }

  public void disableAutoSelection() {
    dialog.getCheckBox("similar").unselect();
  }

  public void checkAutoHideEnabled(boolean enabled) {
    assertEquals(enabled, dialog.getCheckBox("hide").isSelected());
  }

  public void enableAutoHide() {
    dialog.getCheckBox("hide").select();
  }

  public void disableAutoHide() {
    dialog.getCheckBox("hide").unselect();
  }

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }
}
