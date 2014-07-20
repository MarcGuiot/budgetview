package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.DeltaGaugeChecker;
import org.designup.picsou.functests.checkers.components.GaugeChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.gui.components.charts.DeltaGauge;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.series.ui.SeriesPanelUI;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetViewChecker extends ViewChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker variable;
  public final ExtrasBudgetAreaChecker extras;
  public final SavingsBudgetAreaChecker savings;

  public BudgetViewChecker(Window mainWindow) {
    super(mainWindow);
    this.income = new BudgetAreaChecker("incomeBudgetView", BudgetArea.INCOME);
    this.recurring = new BudgetAreaChecker("recurringBudgetView", BudgetArea.RECURRING);
    this.variable = new BudgetAreaChecker("variableBudgetView", BudgetArea.VARIABLE);
    this.extras = new ExtrasBudgetAreaChecker("extrasBudgetView");
    this.savings = new SavingsBudgetAreaChecker("savingsBudgetView");
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

  protected String convert(double amount, BudgetArea budgetArea) {
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

    builder.append(toString(Math.abs(amount)));
    return builder.toString();

  }

  public class BudgetAreaChecker {

    private String panelName;
    private Panel panel;
    private BudgetArea budgetArea;

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
        SavingsViewChecker.toggleToMainIfNeeded(mainWindow);
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

    public BudgetAreaChecker checkTotalGaugeTooltips(String... textElements) {
      GaugeChecker gauge = new GaugeChecker(getPanel(), "totalGauge");
      for (String element : textElements) {
        gauge.checkDescriptionContains(element);
      }
      return this;
    }

    public BudgetAreaChecker checkPlannedUnset(String seriesName) {
      Button button = getSeriesPanel(seriesName).getPlannedAmount();
      assertThat(button.textEquals("To define"));
      return this;
    }

    public BudgetAreaChecker checkPlannedNotHighlighted(String seriesName) {
      Button button = getSeriesPanel(seriesName).getPlannedAmount();
      assertThat(button.backgroundNear("white"));
      return this;
    }

    public BudgetAreaChecker checkPlannedUnsetAndHighlighted(String seriesName) {
      Button button = getSeriesPanel(seriesName).getPlannedAmount();
      assertThat(button.textEquals("To define"));
      assertThat(button.backgroundNear("FFFB00"));
      return this;
    }

    public BudgetAreaChecker checkPlannedUnsetButNotHighlighted(String seriesName) {
      Button button = getSeriesPanel(seriesName).getPlannedAmount();
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

    public BudgetAreaChecker checkSeries(String seriesName, double actual, double plannedAmount) {
      SeriesPanel seriesPanel = getSeriesPanel(seriesName);
      seriesPanel.checkObservedAmount(actual);
      seriesPanel.checkPlannedAmount(plannedAmount);
      return this;
    }

    public BudgetAreaChecker checkSeriesGauge(String seriesName, double actual, double planned) {
      SeriesPanel seriesPanel = getSeriesPanel(seriesName);
      GaugeChecker gauge = seriesPanel.getGauge();
      gauge.checkActualValue(actual);
      gauge.checkTargetValue(planned);
      return this;
    }

    protected SeriesPanel getSeriesPanel(String seriesName) {
      Button seriesButton = getPanel().getButton(seriesName);
      JPanel panel = (JPanel)seriesButton.getContainer().getAwtContainer();
      int index = getIndex(panel, seriesButton.getAwtComponent());
      return new SeriesPanel(panel, index, budgetArea);
    }

    public BudgetAreaChecker checkSeriesDisabled(String seriesName) {
      Button observedAmount = getObservedAmountButton(seriesName);
      assertThat(observedAmount.textEquals("-"));
      assertFalse(observedAmount.isEnabled());

      Button plannedAmount = getSeriesPanel(seriesName).getPlannedAmount();
      assertThat(plannedAmount.textEquals("-"));
      assertFalse(plannedAmount.isEnabled());
      return this;
    }

    public BudgetAreaChecker checkSeriesGaugeRemaining(String seriesName, double remaining, boolean onError) {
      GaugeChecker gauge = getSeriesPanel(seriesName).getGauge();
      gauge.checkRemaining(remaining);
      gauge.checkOnError(onError);
      return this;
    }

    protected String convert(double amount) {
      return BudgetViewChecker.this.convert(amount, budgetArea);
    }

    public BudgetAreaChecker checkNoSeriesShown() {
      checkSeriesList();
      return this;
    }

    public BudgetAreaChecker checkOrder(String... seriesNames) {
      TestUtils.assertEquals(Arrays.asList(seriesNames), getActualNamesList());
      return this;
    }

    public BudgetAreaChecker checkSeriesList(String... expectedNames) {
      TestUtils.assertSetEquals(getActualNamesList(), expectedNames);
      return this;
    }

    private List<String> getActualNamesList() {
      Panel repeatPanel = getPanel().getPanel("seriesRepeat");

      List<String> actualNames = new ArrayList<String>();
      for (Component component : repeatPanel.getSwingComponents(JButton.class, "seriesName")) {
        String text = ((JButton)component).getText();
        actualNames.add(text);
      }
      return actualNames;
    }

    public BudgetAreaChecker checkSeriesPresent(final String... expectedNames) {
      UISpecAssert.assertThat(new Assertion() {
        public void check() {
          List<String> actualNames = getActualNamesList();
          for (String expectedName : expectedNames) {
            if (!actualNames.contains(expectedName)) {
              Assert.fail("Series '" + expectedName + "' not found. Actual series: " + actualNames);
            }
          }
        }
      });
      return this;
    }

    public BudgetAreaChecker checkSeriesNotPresent(final String... seriesNames) {
      UISpecAssert.assertThat(new Assertion() {
        public void check() {
          List<String> actualNames = getActualNamesList();
          for (String expectedName : seriesNames) {
            if (actualNames.contains(expectedName)) {
              Assert.fail("Series '" + expectedName + "' unexpectedly found. Actual series: " + actualNames);
            }
          }
        }
      });
      return this;
    }

    public BudgetAreaChecker checkContent(String expected) {
      Panel seriesRepeat = getPanel().getPanel("seriesRepeat");
      UIComponent[] seriesNames = seriesRepeat.getUIComponents(Button.class, "seriesName");
      UIComponent[] actualAmounts = seriesRepeat.getUIComponents(Button.class, "observedSeriesAmount");
      UIComponent[] plannedAmounts = seriesRepeat.getUIComponents(Button.class, "plannedSeriesAmount");
      TablePrinter table = new TablePrinter();
      for (int i = 0; i < seriesNames.length; i++) {
        table.addRow(seriesNames[i].getLabel(), actualAmounts[i].getLabel(), plannedAmounts[i].getLabel());
      }
      Assert.assertEquals(expected.trim(), table.toString().trim());
      return this;
    }

    public SeriesEditionDialogChecker editSeries(String seriesName) {
      return SeriesEditionDialogChecker.open(
        getSeriesPanel(seriesName).getSeriesButton().triggerClick(Lang.get("series.edit")));
    }

    public void editProjectForSeries(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click(Lang.get("series.edit"));
    }

    public void editProjectForGroup(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click(Lang.get("seriesGroup.goto.project"));
    }

    public SeriesAmountEditionDialogChecker editPlannedAmount(String seriesName) {
      Button button = getSeriesPanel(seriesName).getPlannedAmount();
      return SeriesAmountEditionDialogChecker.open(button.triggerClick());
    }

    public SeriesEditionDialogChecker editPlannedAmountWithFullEditor(String seriesName) {
      Button button = getSeriesPanel(seriesName).getPlannedAmount();
      return SeriesEditionDialogChecker.open(button.triggerClick());
    }

    public void editPlannedAmountForProject(String seriesName) {
      getSeriesPanel(seriesName).getPlannedAmount().click();
    }

    public void deleteSeries(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click(Lang.get("seriesEdition.deleteCurrent"));
    }

    public SeriesDeletionDialogChecker openDeleteSeries(String seriesName) {
      return SeriesDeletionDialogChecker.init(getSeriesPanel(seriesName).getSeriesButton().triggerClick(Lang.get("seriesEdition.deleteCurrent")));
    }

    protected Button getObservedAmountButton(String seriesName) {
      return getSeriesPanel(seriesName).getObservedAmount();
    }

    protected DeltaGaugeChecker getDeltaGauge(String seriesName) {
      return getSeriesPanel(seriesName).getDeltaGauge();
    }

    public BudgetAreaChecker createSeries(String name) {
      createSeries().setName(name).validate();
      return this;
    }

    public BudgetAreaChecker createSeries(String name, String account) {
      createSeries().setName(name).setTargetAccount(account).validate();
      return this;
    }

    public SeriesEditionDialogChecker createSeries() {
      return SeriesEditionDialogChecker.open(getActionPopup().triggerClick(Lang.get("series.add")));
    }

    public BudgetAreaChecker checkAvailableActions(String... actions) {
      getActionPopup().checkChoices(actions);
      return this;
    }

    public BudgetAreaChecker showInactiveSeries() {
      getActionPopup().click(Lang.get("budgetView.actions.disableMonthFiltering"));
      return this;
    }

    public BudgetAreaChecker hideInactiveEnveloppes() {
      getActionPopup().click(Lang.get("budgetView.actions.enableMonthFiltering"));
      return this;
    }

    protected PopupButton getActionPopup() {
      return new PopupButton(getPanel().getButton("seriesActions"));
    }

    public void checkSeriesActions(String seriesName, String... actions) {
      getSeriesPanel(seriesName).getSeriesButton().checkChoices(actions);
    }

    public void gotoData(String seriesName) {
      getObservedAmountButton(seriesName).click();
    }

    public void gotoDataThroughMenu(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click("Show operations");
    }

    public BudgetAreaChecker checkSeriesTooltip(String seriesName, String tooltipText) {
      assertThat(getSeriesPanel(seriesName).getGauge().tooltipContains(tooltipText));
      return this;
    }

    public BudgetAreaChecker checkGaugeTooltip(String seriesName, String... tooltipTextFragments) {
      for (String text : tooltipTextFragments) {
        getSeriesPanel(seriesName).getGauge().checkDescriptionContains(text);
      }
      return this;
    }

    public BudgetAreaChecker checkNameSignpostDisplayed(String seriesName, String text) {
      BudgetViewChecker.this.checkSignpostVisible(mainWindow, getSeriesPanel(seriesName).getGauge(), text);
      return this;
    }

    public BudgetAreaChecker checkGaugeSignpostDisplayed(String seriesName, String text) {
      BudgetViewChecker.this.checkSignpostVisible(mainWindow, getSeriesPanel(seriesName).getGauge().getPanel(), text);
      return this;
    }

    public BudgetAreaChecker checkAmountSignpostDisplayed(String seriesName, String text) {
      BudgetViewChecker.this.checkSingleSignpostVisible(mainWindow, getSeriesPanel(seriesName).getPlannedAmount(), text);
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

    public void gotoAnalysis(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click("See in Analysis view");
    }

    public BudgetAreaChecker checkDeltaGauge(String seriesName, Double previousValue, Double newValue, double ratio, String tooltip) {
      getDeltaGauge(seriesName).check(previousValue, newValue, ratio, tooltip);
      return this;
    }

    public void checkHighlighted(String seriesName) {
      assertTrue(getSeriesPanel(seriesName).getSeriesButton().getButton().backgroundNear("FFFF00"));
    }

    public void checkNotHighlighted(String seriesName) {
      assertTrue(getSeriesPanel(seriesName).getSeriesButton().getButton().backgroundNear("FFFFFF"));
    }

    public void carryExpensesOverdrawOver(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click("Carry expenses overdraw over next month");
    }

    public CarryOverDialogChecker carryExpensesOverdrawOverWithDialog(String seriesName) {
      return CarryOverDialogChecker.open(getExpensesOverdrawTrigger(seriesName));
    }

    public ConfirmationDialogChecker carryExpensesOverdrawOverWithConfirm(String seriesName) {
      return ConfirmationDialogChecker.open(getExpensesOverdrawTrigger(seriesName));
    }

    public MessageDialogChecker carryExpensesOverdrawOverWithMessage(String seriesName) {
      return MessageDialogChecker.open(getExpensesOverdrawTrigger(seriesName));
    }

    private Trigger getExpensesOverdrawTrigger(String seriesName) {
      PopupButton seriesButton = getSeriesPanel(seriesName).getSeriesButton();
      return seriesButton.triggerClick("Carry expenses overdraw over next month");
    }

    public void carryExpensesRemainderOver(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click("Carry expenses remainder over next month");
    }

    public void carryIncomeOverrunOver(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click("Deduce income overrun from next month");
    }

    public void carryIncomeRemainderOver(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().click("Carry income remainder over next month");
    }

    public void checkCarryExpensesOverdrawOverEnabled(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().checkItemEnabled("Carry expenses overdraw over next month");
    }

    public void checkCarryExpensesRemainderOverEnabled(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().checkItemEnabled("Carry expenses remainder over next month");
    }

    public void checkCarryOverNotShown(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().checkItemNotPresent("Carry over next month");
    }

    public void checkCarryOverDisabled(String seriesName) {
      getSeriesPanel(seriesName).getSeriesButton().checkItemDisabled("Carry over next month");
    }

    public BudgetAreaChecker checkAddToGroupOptions(String series, String... labels) {
      getSeriesPanel(series).getSeriesButton().getSubMenu("Add to group")
        .checkChoices(labels);
      return this;
    }

    public SeriesGroupNameDialogChecker addToNewGroup(String series) {
      return SeriesGroupNameDialogChecker.open(getSeriesPanel(series).getSeriesButton()
                                                 .getSubMenu(Lang.get("seriesGroup.menu"))
                                                 .triggerClick(Lang.get("seriesGroup.menu.addToNew")));
    }

    public BudgetAreaChecker addToNewGroup(String series, String group) {
      addToNewGroup(series)
        .setName(group)
        .validate();
      return this;
    }

    public BudgetAreaChecker addToGroup(String series, String group) {
      getSeriesPanel(series).getSeriesButton()
        .getSubMenu(Lang.get("seriesGroup.menu"))
        .click(group);
      return this;
    }

    public BudgetAreaChecker expandGroup(String groupName) {
      getSeriesPanel(groupName).getSeriesButton()
        .click(Lang.get("seriesGroup.menu.expand"));
      return this;
    }

    public BudgetAreaChecker checkGroupToggleExpanded(String groupName) {
      Button toggle = getSeriesPanel(groupName).getToggleButton();
      assertThat(toggle.isEnabled());
      assertThat(toggle.textEquals("-"));
      return this;
    }

    public BudgetAreaChecker collapseGroup(String groupName) {
      getSeriesPanel(groupName).getSeriesButton()
        .click(Lang.get("seriesGroup.menu.collapse"));
      return this;
    }

    public BudgetAreaChecker checkGroupToggleCollapsed(String groupName) {
      Button toggle = getSeriesPanel(groupName).getToggleButton();
      assertThat(toggle.isEnabled());
      assertThat(toggle.textEquals("+"));
      return this;
    }

    public BudgetAreaChecker checkGroupToggleHidden(String groupName) {
      Button toggle = getSeriesPanel(groupName).getToggleButton();
      assertThat(toggle.textEquals(""));
      assertFalse(toggle.isEnabled());
      return this;
    }

    public BudgetAreaChecker clickPlanned(String groupName) {
      getSeriesPanel(groupName).getPlannedAmount().click();
      return this;
    }

    public SeriesGroupNameDialogChecker renameGroup(String series) {
      return SeriesGroupNameDialogChecker.open(getSeriesPanel(series).getSeriesButton()
                                                 .triggerClick(Lang.get("seriesGroup.menu.rename")));
    }

    public void removeFromGroup(String series) {
      getSeriesPanel(series).getSeriesButton()
        .click(Lang.get("seriesGroup.menu.removeFromGroup"));
    }

    public void deleteGroup(String groupName) {
      getSeriesPanel(groupName).getSeriesButton()
        .click(Lang.get("seriesGroup.menu.delete"));
    }

    public void checkGroupItems(String... seriesNames) {
      Panel panel = getPanel().getPanel("seriesRepeat");
      JPanel jPanel = (JPanel)panel.getAwtComponent();
      PanelUI ui = jPanel.getUI();
      if (!(ui instanceof SeriesPanelUI)) {
        Assert.fail("Unexpected panel UI: " + ui);
      }

      SeriesPanelUI seriesPanelUI = (SeriesPanelUI)ui;
      TestUtils.assertSetEquals(seriesPanelUI.getGroupItemLabels(jPanel), seriesNames);
    }

    public void checkPlannedSelected(String... seriesNames) {
      for (String name : seriesNames) {
        JButton plannedButton = getSeriesPanel(name).getPlannedAmount().getAwtComponent();
        if (!plannedButton.getFont().isBold()) {
          fail("Planned amount button for '" + name + " is not selected");
        }

      }
    }

    public void checkPlannedNotSelected(String... seriesNames) {
      for (String name : seriesNames) {
        JButton plannedButton = getSeriesPanel(name).getPlannedAmount().getAwtComponent();
        if (plannedButton.getFont().isBold()) {
          fail("Planned amount button for '" + name + " is unexpectedly selected");
        }
      }
    }
  }

  public class ExtrasBudgetAreaChecker extends BudgetAreaChecker {
    public ExtrasBudgetAreaChecker(String panelName) {
      super(panelName, BudgetArea.EXTRAS);
    }

    public void createProject() {
      getActionPopup().click(Lang.get("projectView.create"));
    }
  }

  public class SavingsBudgetAreaChecker extends BudgetAreaChecker {

    public SavingsBudgetAreaChecker(String panelName) {
      super(panelName, BudgetArea.SAVINGS);
    }

    public void toggleSavingsView() {
      getSpecificActionButton().click();
    }

    public void checkNoToggleSavingsViewSignpostShown() {
      checkNoSignpostVisible(mainWindow);
    }

    public void checkToggleSavingsViewSignpostShown(String text) {
      checkSignpostVisible(getPanel(), getSpecificActionButton(), text);
    }

    private Button getSpecificActionButton() {
      return getPanel().getButton("specificAction");
    }

    public SavingsBudgetAreaChecker createSavingSeries(String name, String fromAccount, String toAccount) {
      createSeries().setName(name).setToAccount(toAccount).setFromAccount(fromAccount).validate();
      return this;
    }
  }

  private class SeriesPanel {

    // The reference component in each row is the gauge
    private static final int SERIES_OFFSET = 0;
    private static final int GROUP_TOGGLE_OFFSET = +1;
    private static final int GAUGE_OFFSET = +2;
    private static final int OBSERVED_LABEL_OFFSET = +3;
    private static final int PLANNED_LABEL_OFFSET = +5;
    private static final int DELTA_GAUGE_OFFSET = +6;

    private JPanel panel;
    private int index;
    private BudgetArea budgetArea;

    private SeriesPanel(JPanel panel, int index, BudgetArea budgetArea) {
      this.panel = panel;
      this.index = index;
      this.budgetArea = budgetArea;
    }

    private PopupButton getSeriesButton() {
      return new PopupButton(new Button((JButton)getComponent(SERIES_OFFSET)));
    }

    private Button getToggleButton() {
      return new Button((JButton)getComponent(GROUP_TOGGLE_OFFSET));
    }

    public GaugeChecker getGauge() {
      return new GaugeChecker((Gauge)getComponent(GAUGE_OFFSET));
    }

    public Button getObservedAmount() {
      return new Button((JButton)getComponent(OBSERVED_LABEL_OFFSET));
    }

    public Button getPlannedAmount() {
      return new Button((JButton)getComponent(PLANNED_LABEL_OFFSET));
    }

    private Component getComponent(int offset) {
      return panel.getComponent(index + offset);
    }

    public void checkObservedAmount(double amount) {
      assertThat(getObservedAmount().textEquals(convert(amount, budgetArea)));
    }

    public void checkPlannedAmount(double amount) {
      assertThat(getPlannedAmount().textEquals(convert(amount, budgetArea)));
    }

    public DeltaGaugeChecker getDeltaGauge() {
      return new DeltaGaugeChecker((DeltaGauge)getComponent(DELTA_GAUGE_OFFSET));
    }
  }
}
