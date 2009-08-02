package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.model.BudgetArea;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class BudgetViewChecker extends GuiChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker envelopes;
  public final BudgetAreaChecker specials;
  public final BudgetAreaChecker savings;

  private Window window;

  public BudgetViewChecker(Window window) {
    this.window = window;
    this.income = new BudgetAreaChecker("incomeBudgetView", true, BudgetArea.INCOME);
    this.recurring = new BudgetAreaChecker("recurringBudgetView", true, BudgetArea.RECURRING);
    this.envelopes = new BudgetAreaChecker("envelopeBudgetView", false, BudgetArea.ENVELOPES);
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

  public BudgetSummaryViewChecker getLabel() {
    return new BudgetSummaryViewChecker(window);
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
    private static final int GAUGE_OFFSET = 2;
    private static final int PLANNED_LABEL_OFFSET = 3;

    public BudgetAreaChecker(String panelName, boolean singleCategorySeries, BudgetArea budgetArea) {
      this.panelName = panelName;
      this.singleCategorySeries = singleCategorySeries;
      this.budgetArea = budgetArea;
    }

    public void checkTitle(String title) {
      Panel budgetPanel = getPanel();
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      assertThat(label.textEquals(title));
    }

    public Panel getPanel() {
      return window.getPanel(panelName);
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
      assertThat(getPanel().getTextBox("totalPlannedAmount").foregroundNear("darkRed"));
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

    public BudgetAreaChecker checkSeriesGaugeRemaining(String seriesName, double remaining) {
      Panel budgetPanel = getPanel();
      Button nameButton = budgetPanel.getButton(seriesName);

      JPanel panel = (JPanel)nameButton.getContainer().getAwtContainer();
      int nameIndex = getIndex(panel, nameButton.getAwtComponent());

      GaugeChecker gauge = new GaugeChecker((Gauge)panel.getComponent(nameIndex + GAUGE_OFFSET));
      gauge.checkOverrunPart(remaining);
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
      if (budgetArea == BudgetArea.SAVINGS) {
        if (amount < 0) {
          builder.append("-");
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

    public BudgetAreaChecker checkSeriesPresent(String... seriesName) {
      Panel budgetPanel = getPanel();
      for (String name : seriesName) {
        UISpecAssert.assertTrue(budgetPanel.containsUIComponent(Button.class, name));
      }
      return this;
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

      return SeriesEditionDialogChecker.open(button);
    }

    public void checkEditAllSeriesIsEnabled(boolean enabled) {
      UISpecAssert.assertEquals(enabled, getPanel().getButton("editAllSeries").isEnabled());
    }

    public BudgetAreaChecker createSeries(String name) {
      createSeries().setName(name).validate();
      return this;
    }

    public SeriesEditionDialogChecker createSeries() {
      return openSeriesEditionDialog("createSeries");
    }

    private SeriesEditionDialogChecker openSeriesEditionDialog(String seriesName) {
      return SeriesEditionDialogChecker.open(getPanel().getButton(seriesName));
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
        assertThat(((Button)component).textEquals(seriesNames[i]));
      }
      return this;
    }

    public void checkSeriesTooltip(String seriesName, String tooltipText) {
      assertThat(getPanel().getButton(seriesName).tooltipEquals(tooltipText));
    }
  }

  public class SavingsAreaChecker extends BudgetAreaChecker {

    public SavingsAreaChecker(String panelName, boolean singleCategorySeries, BudgetArea budgetArea) {
      super(panelName, singleCategorySeries, budgetArea);
    }

    private Set<String> getDisplayedAccounts() {
      UIComponent[] uiComponents = getPanel().getUIComponents(TextBox.class, "accountName");
      Set<String> existingNames = new HashSet<String>();
      for (UIComponent uiComponent : uiComponents) {
        TextBox label = (TextBox)uiComponent;
        if (label.getAwtComponent().isVisible()) {
          existingNames.add(label.getText());
        }
      }
      return existingNames;
    }

    public BudgetAreaChecker checkNoAccountsDisplayed() {
      org.globsframework.utils.TestUtils.assertEmpty(getDisplayedAccounts());
      return this;
    }
  }
}
