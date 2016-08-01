package com.budgetview.functests.checkers.analysis;

import com.budgetview.functests.checkers.components.HistoChartChecker;
import com.budgetview.functests.checkers.components.StackChecker;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetAnalysisChecker {
  private Panel panel;
  public final HistoChartChecker histoChart;
  public final StackChecker balanceChart;
  public final StackChecker seriesChart;
  public final StackChecker groupSeriesChart;
  public final StackChecker subSeriesChart;

  private static final String PANEL_NAME = "budgetAnalysis";

  public BudgetAnalysisChecker(Window mainWindow) {
    this.panel = mainWindow;
    this.histoChart = new HistoChartChecker(mainWindow, "budgetAnalysis", "histoChart");
    this.balanceChart = new StackChecker(mainWindow, PANEL_NAME, "balanceChart");
    this.seriesChart = new StackChecker(mainWindow, PANEL_NAME, "seriesChart");
    this.groupSeriesChart = new StackChecker(mainWindow, PANEL_NAME, "groupChart");
    this.subSeriesChart = new StackChecker(mainWindow, PANEL_NAME, "subSeriesChart");
  }

  public void checkBreadcrumb(String text) {
    assertThat(panel.getTextBox("breadcrumb").textEquals(text));
  }

  public void clickBreadcrumb(String link) {
    panel.getTextBox("breadcrumb").clickOnHyperlink(link);
  }

  public BudgetAnalysisChecker checkHistoChartLabel(String text) {
    TextBox textBox = panel.getTextBox("histoChartLabel");
    Assert.assertEquals(text, org.uispec4j.utils.Utils.cleanupHtml(textBox.getText()));
    return this;
  }

  public BudgetAnalysisChecker checkBalanceChartLabel(String text) {
    TextBox textBox = panel.getTextBox("balanceChartLabel");
    Assert.assertEquals(text, org.uispec4j.utils.Utils.cleanupHtml(textBox.getText()));
    return this;
  }

  public BudgetAnalysisChecker checkSeriesChartLabel(String text) {
    TextBox textBox = panel.getTextBox("seriesChartLabel");
    Assert.assertEquals(text, org.uispec4j.utils.Utils.cleanupHtml(textBox.getText()));
    return this;
  }

  public BudgetAnalysisChecker checkLegendShown(String lineText, String fillText) {
    Panel legendPanel = panel.getPanel("histoChartLegend");
    assertThat(legendPanel.isVisible());
    assertThat(legendPanel.getTextBox("lineLabelText").textEquals(lineText));
    assertThat(legendPanel.getTextBox("fillLabelText").textEquals(fillText));
    return this;
  }

  public BudgetAnalysisChecker checkLegendHidden() {
    Panel legendPanel = panel.getPanel("histoChartLegend");
    assertFalse(legendPanel.isVisible());
    return this;
  }

  public BudgetAnalysisChecker checkGroupSeriesAndSubSeriesStacksShown() {
    balanceChart.checkHidden();
    seriesChart.checkHidden();
    groupSeriesChart.checkVisible();
    subSeriesChart.checkVisible();
    return this;
  }

  public BudgetAnalysisChecker checkSeriesAndGroupSeriesStacksShown() {
    balanceChart.checkHidden();
    seriesChart.checkVisible();
    groupSeriesChart.checkVisible();
    subSeriesChart.checkHidden();
    return this;
  }

  public BudgetAnalysisChecker checkBudgetAndSeriesStacksShown() {
    balanceChart.checkVisible();
    seriesChart.checkVisible();
    groupSeriesChart.checkHidden();
    subSeriesChart.checkHidden();
    return this;
  }

  public BudgetAnalysisChecker checkSubSeriesStackShown() {
    balanceChart.checkHidden();
    seriesChart.checkVisible();
    subSeriesChart.checkVisible();
    return this;
  }

  public BudgetAnalysisChecker checkGotoBudgetShown() {
    Button gotoUpButton = panel.getButton("gotoUpButton");
    assertTrue(gotoUpButton.isVisible());
    assertTrue(gotoUpButton.textEquals(Lang.get("seriesAnalysis.toggleController.gotoBudget")));
    return this;
  }

  public BudgetAnalysisChecker checkGotoUpHidden() {
    assertFalse(panel.getButton("gotoUpButton").isVisible());
    return this;
  }

  public BudgetAnalysisChecker checkGotoDownHidden() {
    assertFalse(panel.getButton("gotoDownButton").isVisible());
    return this;
  }

  public BudgetAnalysisChecker checkGotoDownToGroupSeriesShown() {
    Button gotoDownButton = panel.getButton("gotoDownButton");
    assertTrue(gotoDownButton.isVisible());
    assertTrue(gotoDownButton.textEquals(Lang.get("seriesAnalysis.toggleController.gotoGroupSeries")));
    return this;
  }

  public BudgetAnalysisChecker checkGotoUpToGroupSeriesShown() {
    Button gotoUpButton = panel.getButton("gotoUpButton");
    assertTrue(gotoUpButton.isVisible());
    assertTrue(gotoUpButton.textEquals(Lang.get("seriesAnalysis.toggleController.gotoGroupSeries")));
    return this;
  }

  public BudgetAnalysisChecker checkGotoSubSeriesShown() {
    Button gotoDownButton = panel.getButton("gotoDownButton");
    assertTrue(gotoDownButton.isVisible());
    assertTrue(gotoDownButton.textEquals(Lang.get("seriesAnalysis.toggleController.gotoSubSeries")));
    return this;
  }

  public BudgetAnalysisChecker checkStackButtonsHidden() {
    assertFalse(panel.getButton("gotoUpButton").isVisible());
    assertFalse(panel.getButton("gotoDownButton").isVisible());
    return this;
  }

  public BudgetAnalysisChecker gotoUp() {
    Button button = panel.getButton("gotoUpButton");
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    button.click();
    return this;
  }

  public BudgetAnalysisChecker gotoDown() {
    Button button = panel.getButton("gotoDownButton");
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    button.click();
    return this;
  }
}
