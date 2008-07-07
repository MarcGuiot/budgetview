package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CategorizationDialogChecker extends DataChecker {
  private Window dialog;
  private TextBox transactionLabel;

  public CategorizationDialogChecker(Window dialog) {
    this.dialog = dialog;
    this.transactionLabel = dialog.getTextBox("transactionLabel");
  }

  public void checkLabel(String expected) {
    assertTrue(transactionLabel.textEquals(expected));
  }

  public void checkBudgetAreaIsSelected(BudgetArea budgetArea) {
    assertTrue(dialog.getToggleButton(budgetArea.getGlob().get(BudgetArea.NAME)).isSelected());
  }

  public void selectIncome() {
    dialog.getToggleButton(BudgetArea.INCOME.getGlob().get(BudgetArea.NAME)).click();
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

  public void selectIncomeSeries(String name) {
    Panel panel = getIncomeSeriesPanel();
    panel.getToggleButton(name).click();
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

  public void selectRecurringSeries(String name) {
    Panel panel = getRecurringSeriesPanel();
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

  public void selectEnvelopeSeries(String envelopeName, MasterCategory category) {
    Panel panel = getEnvelopeSeriesPanel();
    panel.getToggleButton(envelopeName + ":" + category.getName()).click();
  }

  private Panel getEnvelopeSeriesPanel() {
    Panel panel = dialog.getPanel("envelopeSeriesRepeat");
    assertTrue(panel.isVisible());
    return panel;
  }

  public void selectOccasional() {
    dialog.getToggleButton("occasionalExpenses").click();
  }

  public void selectOccasionalSeries(MasterCategory category) {
    getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).click();
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

  public void checkOccasionalSeries(MasterCategory category) {
    assertTrue(dialog.getToggleButton("occasionalExpenses").isSelected());
    assertTrue(getOccasionalSeriesPanel().getToggleButton("occasionalSeries" + ":" + category.getName()).isSelected());
  }

  public void checkEnveloppeSeriesIsSelected(String seriesName, MasterCategory category) {
    assertTrue(dialog.getToggleButton("expensesEnvelope").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    assertTrue(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
  }

  public void checkEnveloppeSeriesIsNotSelected(String seriesName, MasterCategory category) {
    assertTrue(dialog.getToggleButton("expensesEnvelope").isSelected());
    Panel panel = getEnvelopeSeriesPanel();
    UISpecAssert.assertFalse(panel.getToggleButton(seriesName + ":" + category.getName()).isSelected());
  }

  public void checkPreviousIsDisabled() {
    UISpecAssert.assertFalse(dialog.getButton("previousTransaction").isEnabled());
  }

  public void checkNextIsDisable() {
    UISpecAssert.assertFalse(dialog.getButton("nextTransaction").isEnabled());
  }

  public void selectNext() {
    dialog.getButton("nextTransaction").click();
  }

  public void selectPrevious() {
    dialog.getButton("previousTransaction").click();
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
}
