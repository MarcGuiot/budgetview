package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

public class BudgetViewChecker extends DataChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker envelopes;
  public final OccasionalAreaChecker occasional;
  public final BudgetAreaChecker specials;
  public final BudgetAreaChecker savings;

  private Window window;

  public BudgetViewChecker(Window window) {
    this.window = window;
    this.income = new BudgetAreaChecker("incomeBudgetView", true, BudgetArea.INCOME);
    this.recurring = new BudgetAreaChecker("recurringBudgetView", true, BudgetArea.RECURRING);
    this.envelopes = new BudgetAreaChecker("envelopeBudgetView", false, BudgetArea.ENVELOPES);
    this.occasional = new OccasionalAreaChecker();
    this.specials = new BudgetAreaChecker("projectsBudgetView", false, BudgetArea.SPECIAL);
    this.savings = new BudgetAreaChecker("savingsBudgetView", true, BudgetArea.SAVINGS);
  }

  private int getIndex(JPanel panel, Component component) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (component == panel.getComponent(i)) {
        return i;
      }
    }
    return -1;
  }

  public BudgetViewChecker checkBalance(double free) {
    return this;
  }

  public BudgetLabelChecker getLabel() {
    return new BudgetLabelChecker(window);
  }

  public void checkHelpMessageDisplayed(boolean visible) {
    checkComponentVisible(window.getPanel("budgetView"), JEditorPane.class, "helpMessage", visible);
  }

  public void hideHelpMessage() {
    window.getPanel("budgetView").getButton("hideHelpMessage").click();
  }

  public class BudgetAreaChecker {

    private String panelName;
    private boolean singleCategorySeries;
    private BudgetArea budgetArea;

    private static final int OBSERVED_LABEL_OFFSET = 1;
    private static final int PLANNED_LABEL_OFFSET = 3;

    public BudgetAreaChecker(String panelName, boolean singleCategorySeries, BudgetArea budgetArea) {
      this.panelName = panelName;
      this.singleCategorySeries = singleCategorySeries;
      this.budgetArea = budgetArea;
    }

    public void checkTitle(String title) {
      Panel budgetPanel = getPanel();
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      UISpecAssert.assertThat(label.textEquals(title));
    }

    private Panel getPanel() {
      return window.getPanel(panelName);
    }

    public BudgetAreaChecker checkTotalAmounts(double observed, double planned) {
      Panel budgetPanel = getPanel();
      TextBox totalObserved = budgetPanel.getTextBox("totalObservedAmount");
      UISpecAssert.assertTrue(totalObserved.textEquals(convert(observed)));

      TextBox totalPlanned = budgetPanel.getTextBox("totalPlannedAmount");
      UISpecAssert.assertTrue(totalPlanned.textEquals(convert(planned)));
      return this;
    }

    public BudgetAreaChecker checkTotalGauge(double actual, double target) {
      GaugeChecker gauge = new GaugeChecker(getPanel(), "totalGauge");
      gauge.checkActualValue(actual);
      gauge.checkTargetValue(target);
      return this;
    }

    public BudgetAreaChecker checkTotalPositiveOverrun() {
      UISpecAssert.assertThat(getPanel().getTextBox("totalPlannedAmount").foregroundNear("0033AA"));
      return this;
    }

    public BudgetAreaChecker checkTotalErrorOverrun() {
      UISpecAssert.assertThat(getPanel().getTextBox("totalPlannedAmount").foregroundNear("darkRed"));
      return this;
    }

    public BudgetAreaChecker checkSeries(String seriesName, double observedAmount, double plannedAmount) {
      Panel budgetPanel = getPanel();
      Button nameButton = budgetPanel.getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());

      checkAmount("observed", OBSERVED_LABEL_OFFSET, seriesName, observedAmount, panel, nameIndex);
      checkAmount("planned", PLANNED_LABEL_OFFSET, seriesName, plannedAmount, panel, nameIndex);
      return this;
    }

    private void checkAmount(String label, int offset,
                             String seriesName, double expectedAmount,
                             JPanel panel, int nameIndex) {
      Button button = new Button((JButton)panel.getComponent(nameIndex + offset));
      String expectedAmountText = convert(expectedAmount);
      UISpecAssert.assertTrue(seriesName + " " + label + ":\nExpected :" + expectedAmountText +
                              "\nActual   :" + button.getLabel(),
                              button.textEquals(expectedAmountText));
    }

    private String convert(double amount) {
      StringBuilder builder = new StringBuilder();
      if (budgetArea.isIncome()) {
        builder.append(amount < 0 ? "-" : "");
      }
      else {
        builder.append(amount > 0 ? "+" : "");
      }

      builder.append(BudgetViewChecker.this.toString(Math.abs(amount)));
      return builder.toString();
    }

    public BudgetAreaChecker checkSeriesNotPresent(String... seriesName) {
      Panel budgetPanel = getPanel();
      for (String name : seriesName) {
        UISpecAssert.assertFalse(budgetPanel.containsUIComponent(Button.class, name));
      }
      return this;
    }

    public SeriesEditionDialogChecker editSeriesList() {
      return openSeriesEditionDialog("editAllSeries");
    }

    public SeriesEditionDialogChecker editSeries(String seriesName) {
      return openSeriesEditionDialog(seriesName);
    }

    public SeriesEditionDialogChecker clickOnPlannedAmount(String seriesName) {
      Button nameButton = getPanel().getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());

      Button button = new Button((JButton)panel.getComponent(nameIndex + PLANNED_LABEL_OFFSET));

      Window dialog = WindowInterceptor.getModalDialog(button.triggerClick());
      return new SeriesEditionDialogChecker(dialog, singleCategorySeries);
    }

    public void checkEditAllSeriesIsEnabled(boolean enabled) {
      UISpecAssert.assertEquals(enabled, getPanel().getButton("editAllSeries").isEnabled());
    }

    public SeriesEditionDialogChecker createSeries() {
      return openSeriesEditionDialog("createSeries");
    }

    private SeriesEditionDialogChecker openSeriesEditionDialog(String seriesName) {
      Window dialog = WindowInterceptor.getModalDialog(getPanel().getButton(seriesName).triggerClick());
      return new SeriesEditionDialogChecker(dialog, singleCategorySeries);
    }

    public void gotoData(String seriesName) {
      Button nameButton = getPanel().getButton(seriesName);
      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());
      Button button = new Button((JButton)panel.getComponent(nameIndex + OBSERVED_LABEL_OFFSET));
      button.click();
    }

    public BudgetAreaChecker checkOrder(String... seriesNames) {
      UIComponent[] uiComponents = getPanel().getUIComponents(Button.class, "seriesName");
      Assert.assertEquals(uiComponents.length, seriesNames.length);
      for (int i = 0; i < uiComponents.length; i++) {
        UIComponent component = uiComponents[i];
        UISpecAssert.assertThat(((Button)component).textEquals(seriesNames[i]));
      }
      return this;
    }
  }

  public class OccasionalAreaChecker {

    public void checkTitle(String title) {
      Panel budgetPanel = getPanel();
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      UISpecAssert.assertThat(label.textEquals(title));
    }

    public void checkTotalAmount(double spent, double free) {
      Panel budgetPanel = getPanel();
      TextBox totalObserved = budgetPanel.getTextBox("totalObservedAmount");
      String observedAmount = BudgetViewChecker.this.toString(spent);
      if (spent < 0.0) {
        observedAmount = observedAmount.replace("-", "+");
      }
      UISpecAssert.assertTrue(totalObserved.textEquals(observedAmount));

      TextBox totalPlanned = budgetPanel.getTextBox("totalPlannedAmount");
      String amount = BudgetViewChecker.this.toString(free);
      if (free < 0.0) {
        amount = amount.replace("-", "+");
      }
      UISpecAssert.assertTrue(totalPlanned.textEquals(amount));
    }

    public OccasionalAreaChecker check(MasterCategory category, Double amount) {
      Panel budgetPanel = getPanel();
      UISpecAssert.assertTrue(budgetPanel.containsUIComponent(TextBox.class, "categoryName." + getCategoryName(category)));
      UISpecAssert.assertTrue(budgetPanel.getButton("amount." + getCategoryName(category))
        .textEquals(BudgetViewChecker.this.toString(amount)));
      return this;
    }

    public OccasionalAreaChecker checkNotDisplayed(MasterCategory master) {
      return checkNotDisplayed(getCategoryName(master));
    }

    public OccasionalAreaChecker checkNotDisplayed(String categoryName) {
      Panel budgetPanel = getPanel();
      UISpecAssert.assertFalse(budgetPanel.containsUIComponent(TextBox.class, "categoryName." + categoryName));
      return this;
    }

    public void gotoData(MasterCategory master) {
      Panel budgetPanel = getPanel();
      UISpecAssert.assertTrue(budgetPanel.containsUIComponent(TextBox.class, "categoryName." + getCategoryName(master)));
      budgetPanel.getButton("amount." + getCategoryName(master)).click();
    }

    public OccasionalAreaChecker checkOrder(MasterCategory... categories) {
      Panel budgetPanel = getPanel();
      UIComponent[] uiComponents = budgetPanel.getUIComponents(TextBox.class, "categoryName");
      Assert.assertEquals(uiComponents.length, categories.length);
      for (int i = 0; i < uiComponents.length; i++) {
        UIComponent component = uiComponents[i];
        UISpecAssert.assertThat(((TextBox)component).textEquals(getCategoryName(categories[i])));
      }
      return this;
    }

    private Panel getPanel() {
      return window.getPanel("occasionalBudgetView");
    }

    public OccasionalSerieEditionChecker edit() {
      return new OccasionalSerieEditionChecker(
        WindowInterceptor.getModalDialog(getPanel().getButton("editOccasionalSeries").triggerClick()));
    }
  }
}
