package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.JPopupButtonChecker;
import org.designup.picsou.functests.utils.BalloonTipTesting;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetViewChecker extends ViewChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker variable;
  public final ExtrasBudgetAreaChecker extras;
  public final BudgetAreaChecker savings;

  public BudgetViewChecker(Window mainWindow) {
    super(mainWindow);
    this.income = new BudgetAreaChecker("incomeBudgetView", BudgetArea.INCOME);
    this.recurring = new BudgetAreaChecker("recurringBudgetView", BudgetArea.RECURRING);
    this.variable = new BudgetAreaChecker("variableBudgetView", BudgetArea.VARIABLE);
    this.extras = new ExtrasBudgetAreaChecker("extrasBudgetView");
    this.savings = new BudgetAreaChecker("savingsBudgetView", BudgetArea.SAVINGS);
  }

  private int getIndex(JPanel panel, Component component) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (component == panel.getComponent(i)) {
        return i;
      }
    }
    return -1;
  }

  public BudgetSummaryViewChecker getSummary() {
    views.selectBudget();
    return new BudgetSummaryViewChecker(mainWindow);
  }

  public class BudgetAreaChecker {

    private String panelName;
    private Panel panel;
    private BudgetArea budgetArea;

    private static final int OBSERVED_LABEL_OFFSET = 1;
    private static final int GAUGE_OFFSET = 2;
    private static final int PLANNED_LABEL_OFFSET = 3;

    public BudgetAreaChecker(String panelName, BudgetArea budgetArea) {
      this.panelName = panelName;
      this.budgetArea = budgetArea;
    }

    public void checkTitle(String title) {
      Panel budgetPanel = getPanel();
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      assertThat(label.textEquals(title));
    }

    public Panel getPanel() {
      views.selectBudget();
      if (panel == null) {
        panel = mainWindow.getPanel(panelName);
      }
      return panel;
    }

    public BudgetAreaChecker checkTotalAmounts(double observed, double planned) {
      checkTotalObserved(observed);
      checkTotalPlanned(planned);
      return this;
    }

    public BudgetAreaChecker checkTotalObserved(double observed) {
      TextBox totalObserved = getPanel().getTextBox("totalObservedAmount");
      UISpecAssert.assertTrue(totalObserved.textEquals(convert(observed)));
      return this;
    }

    public BudgetAreaChecker checkTotalPlanned(double planned) {
      TextBox totalPlanned = getPanel().getTextBox("totalPlannedAmount");
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
      assertThat(getPanel().getTextBox("totalPlannedAmount").foregroundNear("0033AA"));
      return this;
    }

    public BudgetAreaChecker checkTotalErrorOverrun() {
      assertThat(getPanel().getTextBox("totalPlannedAmount").foregroundNear("red"));
      return this;
    }

    public BudgetAreaChecker checkGaugeBeginInError() {
      GaugeChecker gauge = new GaugeChecker(getPanel(), "totalGauge");
      gauge.checkBeginInError();
      return this;
    }

    public BudgetAreaChecker checkTotalDescription(double remaining, double overrun, double newAmount) {
      GaugeChecker gauge = new GaugeChecker(getPanel(), "totalGauge");
      gauge.checkDescriptionContains(Integer.toString((int)remaining));
      gauge.checkDescriptionContains(Integer.toString((int)overrun));
      assertThat(getPanel().getTextBox("totalPlannedAmount").tooltipContains(Double.toString(newAmount)));
      return this;
    }

    public BudgetAreaChecker checkTotalGaugeTooltips(String... text) {
      GaugeChecker gauge = new GaugeChecker(getPanel(), "totalGauge");
      for (String s : text) {
        gauge.checkDescriptionContains(s);
      }
      return this;
    }

    public BudgetAreaChecker checkPlannedUnset(String seriesName) {
      Button button = getPlannedAmountButton(seriesName);
      assertThat(button.textEquals("To define"));
      return this;
    }

    public BudgetAreaChecker checkPlannedNotHighlighted(String seriesName) {
      Button button = getPlannedAmountButton(seriesName);
      assertThat(button.backgroundNear("white"));
      return this;
    }

    public BudgetAreaChecker checkPlannedUnsetAndHighlighted(String seriesName) {
      Button button = getPlannedAmountButton(seriesName);
      assertThat(button.textEquals("To define"));
      assertThat(button.backgroundNear("yellow"));
      return this;
    }

    public BudgetAreaChecker checkPlannedUnsetButNotHighlighted(String seriesName) {
      Button button = getPlannedAmountButton(seriesName);
      assertThat(button.textEquals("To define"));
      assertThat(button.backgroundNear("white"));
      return this;
    }

    public BudgetAreaChecker checkTotalTooltips(double overrun, double newAmount) {
      assertThat(getPanel().getTextBox("totalPlannedAmount")
                   .tooltipEquals("Planned with overrun: " + Formatting.DECIMAL_FORMAT.format(newAmount) +
                                  " - Overrun: " + Formatting.DECIMAL_FORMAT.format(overrun)));
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

    public BudgetAreaChecker checkSeriesDisabled(String seriesName) {
      Panel budgetPanel = getPanel();
      Button nameButton = budgetPanel.getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());
      Button observedAmount = getButton(panel, nameIndex, OBSERVED_LABEL_OFFSET);
      assertThat(observedAmount.textEquals("-"));
      assertFalse(observedAmount.isEnabled());

      Button plannedAmount = getButton(panel, nameIndex, PLANNED_LABEL_OFFSET);
      assertThat(plannedAmount.textEquals("-"));
      assertFalse(plannedAmount.isEnabled());
      return this;
    }

    public BudgetAreaChecker checkSeriesGaugeRemaining(String seriesName, double remaining, boolean onError) {
      GaugeChecker gauge = getGauge(seriesName);
      gauge.checkRemaining(remaining);
      gauge.checkOnError(onError);
      return this;
    }

    private GaugeChecker getGauge(String seriesName) {
      Panel budgetPanel = getPanel();
      Button nameButton = budgetPanel.getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());

      return new GaugeChecker((Gauge)panel.getComponent(nameIndex + GAUGE_OFFSET));
    }

    private void checkAmount(String label, int offset,
                             String seriesName, double expectedAmount,
                             JPanel panel, int nameIndex) {
      Button button = getButton(panel, nameIndex, offset);
      String expectedAmountText = convert(expectedAmount);
      UISpecAssert.assertTrue(seriesName + " " + label + ":\nExpected :" + expectedAmountText +
                              "\nActual   :" + button.getLabel(),
                              button.textEquals(expectedAmountText));
    }

    private Button getButton(JPanel panel, int nameIndex, int offset) {
      return new Button((JButton)panel.getComponent(nameIndex + offset));
    }

    private String convert(double amount) {
      StringBuilder builder = new StringBuilder();
      if (budgetArea == BudgetArea.SAVINGS) {
        if (amount < 0) {
          builder.append("+");
        }
      }
      else if (budgetArea.isIncome()) {
        builder.append(amount < 0 ? "-" : "");
      }
      else {
        builder.append(amount > 0 ? "+" : "");
      }

      builder.append(BudgetViewChecker.this.toString(Math.abs(amount)));
      return builder.toString();
    }

    public BudgetAreaChecker checkNoSeriesShown() {
      checkSeriesList();
      return this;
    }

    public BudgetAreaChecker checkOrder(String... seriesNames) {
      UIComponent[] seriesButtons = getPanel().getUIComponents(Button.class, "seriesName");
      java.util.List<String> actualNames = new ArrayList<String>();
      for (UIComponent button : seriesButtons) {
        actualNames.add(button.getLabel());
      }
      TestUtils.assertEquals(Arrays.asList(seriesNames), actualNames);
      return this;
    }

    public BudgetAreaChecker checkSeriesList(String... expectedNames) {
      UIComponent[] components = getPanel().getPanel("seriesRepeat").getUIComponents(Button.class, "seriesName");
      java.util.List<String> actualNames = new ArrayList<String>();
      for (UIComponent component : components) {
        actualNames.add(component.getLabel());
      }
      TestUtils.assertSetEquals(actualNames, expectedNames);
      return this;
    }

    public BudgetAreaChecker checkSeriesPresent(String... expectedNames) {
      Panel budgetPanel = getPanel();
      for (String name : expectedNames) {
        UISpecAssert.assertTrue(budgetPanel.containsUIComponent(Button.class, name));
      }
      return this;
    }

    public BudgetAreaChecker checkSeriesNotPresent(String... seriesNames) {
      Panel budgetPanel = getPanel();
      for (String name : seriesNames) {
        assertFalse(budgetPanel.containsUIComponent(Button.class, name));
      }
      return this;
    }

    public SeriesEditionDialogChecker editSeries(String seriesName) {
      return openSeriesEditionDialog(seriesName);
    }

    public SeriesAmountEditionDialogChecker editPlannedAmount(String seriesName) {
      Button button = getPlannedAmountButton(seriesName);

      return SeriesAmountEditionDialogChecker.open(button.triggerClick());
    }

    protected Button getPlannedAmountButton(String seriesName) {
      Button nameButton = getPanel().getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());

      return new Button((JButton)panel.getComponent(nameIndex + PLANNED_LABEL_OFFSET));
    }

    public BudgetAreaChecker createSeries(String name) {
      createSeries().setName(name).validate();
      return this;
    }

    public SeriesEditionDialogChecker createSeries() {
      return SeriesEditionDialogChecker.open(getActionPopup().triggerClick(Lang.get("series.add")));
    }

    public BudgetAreaChecker checkAvailableActions(String... actions) {
      getActionPopup().checkChoices(actions);
      return this;
    }

    public BudgetAreaChecker showInactiveEnveloppes() {
      getActionPopup().click(Lang.get("budgetView.actions.disableMonthFiltering"));
      return this;
    }

    public BudgetAreaChecker hideInactiveEnveloppes() {
      getActionPopup().click(Lang.get("budgetView.actions.enableMonthFiltering"));
      return this;
    }

    private SeriesEditionDialogChecker openSeriesEditionDialog(String seriesName) {
      return SeriesEditionDialogChecker.open(getPanel().getButton(seriesName));
    }

    protected JPopupButtonChecker getActionPopup() {
      return new JPopupButtonChecker(getPanel().getButton("seriesActions"));
    }

    public void gotoData(String seriesName) {
      Button nameButton = getPanel().getButton(seriesName);
      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());
      Button button = getButton(panel, nameIndex, OBSERVED_LABEL_OFFSET);
      button.click();
    }


    public BudgetAreaChecker checkSeriesTooltip(String seriesName, String tooltipText) {
      assertThat(getPanel().getButton(seriesName).tooltipContains(tooltipText));
      return this;
    }

    public void clickGaugeAndCloseTip(String seriesName) {
      GaugeChecker gauge = getGauge(seriesName);
      gauge.click();
      BalloonTipTesting.closeTip(mainWindow, gauge.getPanel());

    }

    public BudgetAreaChecker checkGaugeTooltip(String seriesName, String... tooltipTextFragments) {
      for (String text : tooltipTextFragments) {
        getGauge(seriesName).checkDescriptionContains(text);
      }
      return this;
    }

    public BudgetAreaChecker checkNameSignpostDisplayed(String seriesName, String text) {
      Button nameButton = getPanel().getButton(seriesName);
      BudgetViewChecker.this.checkSignpostVisible(mainWindow, nameButton, text);
      return this;
    }

    public BudgetAreaChecker checkGaugeSignpostDisplayed(String seriesName, String text) {
      BudgetViewChecker.this.checkSignpostVisible(mainWindow, getGauge(seriesName).getPanel(), text);
      return this;
    }

    public BudgetAreaChecker checkAmountSignpostDisplayed(String seriesName, String text) {
      BudgetViewChecker.this.checkSignpostVisible(mainWindow, getPlannedAmountButton(seriesName), text);
      return this;
    }

    public BudgetAreaChecker clickTitleSeriesName() {
      Button button = getPanel().getButton("titleSeries");
      button.click();
      return this;
    }

    public BudgetAreaChecker clickTitleRealAmount() {
      Button button = getPanel().getButton("titleAmountReal");
      button.click();
      return this;
    }

    public BudgetAreaChecker clickTitlePlannedAmount() {
      Button button = getPanel().getButton("titleAmountPlanned");
      button.click();
      return this;
    }

    public BudgetAreaChecker align(String seriesName) {
      editSeries(seriesName).alignPlannedAndActual().validate();
      return this;
    }

    public BudgetAreaChecker alignAndPropagate(String seriesName) {
      editSeries(seriesName).alignPlannedAndActual().setPropagationEnabled().validate();
      return this;
    }
  }

  public class ExtrasBudgetAreaChecker extends BudgetAreaChecker {
    public ExtrasBudgetAreaChecker(String panelName) {
      super(panelName, BudgetArea.EXTRAS);
    }

    public ProjectEditionChecker editProjectSeries(String seriesName) {
      return ProjectEditionChecker.open(getPanel().getButton(seriesName));
    }

    public ProjectEditionChecker editPlannedAmountForProject(String seriesName) {
      return ProjectEditionChecker.open(getPlannedAmountButton(seriesName));
    }

    public ProjectEditionChecker createProject() {
      return ProjectEditionChecker.open(getActionPopup().triggerClick(Lang.get("projectView.create")));
    }
  }
}
