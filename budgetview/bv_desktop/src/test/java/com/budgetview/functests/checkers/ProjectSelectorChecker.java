package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.HistoButtonChartChecker;
import com.budgetview.functests.checkers.components.HistoDailyChecker;
import com.budgetview.functests.checkers.components.PopupButton;
import com.budgetview.functests.checkers.components.PopupChecker;
import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.globsframework.gui.splits.utils.HtmlUtils;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectSelectorChecker extends ViewChecker {
  private Panel panel;
  private HistoButtonChartChecker chart;

  public ProjectSelectorChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkShowsCreation() {
    checkComponentVisible(getPanel(), JButton.class, "createFirstProject", true);
    checkComponentVisible(getPanel(), JButton.class, "createProject", false);
    checkComponentVisible(getPanel(), HistoChart.class, "projectChart", false);
  }

  public void checkShowsChart() {
    checkComponentVisible(getPanel(), JButton.class, "createFirstProject", false);
    checkComponentVisible(getPanel(), JButton.class, "createProject", true);
    checkComponentVisible(getPanel(), HistoChart.class, "projectChart", true);
  }

  public void create() {
    Button createProject = getPanel().getButton("createProject");
    assertThat("This button is not yet visible - use createFirst() if no project exists", createProject.isVisible());
    createProject.click();
  }

  public void createFirst() {
    getPanel().getButton("createFirstProject").click();
  }

  public ConfirmationDialogChecker createFirstAndOpenConfirmation() {
    return ConfirmationDialogChecker.open(getPanel().getButton("createFirstProject").triggerClick());
  }

  public ProjectSelectorChecker checkRange(int firstMonth, int lastMonth) {
    getChart().checkRange(firstMonth, lastMonth);
    return this;
  }

  public void select(String projectName) {
    views.selectHome();
    getChart().click(projectName, false);
  }

  public void selectMonth(int monthId) {
    getChart().clickColumnId(monthId);
  }

  public ProjectSelectorChecker checkNoProjectShown() {
    views.selectProjects();
    getChart().checkNoElementShown();
    return this;
  }

  public ProjectSelectorChecker checkProjectList(String... projectNames) {
    views.selectProjects();
    getChart().checkElementNames(projectNames);
    return this;
  }

  public ProjectSelectorChecker checkProject(String projectName, int start, int end, double amount) {
    getChart().checkElementPeriod(projectName, start, end);
    getChart().checkElementTooltipContains(projectName, "Planned: " + toString(amount));
    return this;
  }

  public HistoButtonChartChecker getChart() {
    if (chart == null) {
      views.selectProjects();
      chart = new HistoButtonChartChecker(mainWindow, "projectSelector", "projectChart");
    }
    return chart;
  }

  public ProjectDuplicationDialogChecker duplicate(String project) {
    PopupChecker popup = openPopup(project);
    return ProjectDuplicationDialogChecker.open(popup.triggerClick("Duplicate..."));
  }

  private PopupChecker openPopup(final String project) {
    return new PopupChecker() {
      protected org.uispec4j.MenuItem openMenu() {
        return PopupMenuInterceptor.run(getChart().triggerRightClick(project));
      }
    };
  }

  public HistoDailyChecker getAccountChart(String name) {
    return new HistoDailyChecker(getAccountPanel(name), "accountHistoChart");
  }

  public Panel getAccountPanel(String name) {
    views.selectProjects();
    Button button = getPanel().getButton(name);
    return button.getContainer("accountPanel");
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectProjects();
      panel = mainWindow.getPanel("projectSelector");
    }
    return panel;
  }

  public void checkNoAccounts() {
    checkAccounts();
  }

  public void checkAccounts(String... accountNames) {
    List<String> result = new ArrayList<String>();
    for (UIComponent component : getPanel().getUIComponents(Button.class, "accountChartButton")) {
      result.add(component.getLabel());
    }
    TestUtils.assertEquals(result, accountNames);
  }

  public void moveAccountUp(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.move.up"));
  }

  public void moveAccountDown(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.move.down"));
  }

  public void hideGraph(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.chart.show"));
  }

  public void showGraph(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.chart.show"));
  }

  public void checkGraphShown(String accountName) {
    Assert.assertTrue(getAccountChart(accountName).getChart().isVisible());
  }

  public void checkGraphHidden(String accountName) {
    Assert.assertFalse(getAccountChart(accountName).getChart().isVisible());
  }

  public void checkAccountPosition(String accountName, String expected) {
    Assert.assertEquals(expected,
                        HtmlUtils.cleanup(getAccountPanel(accountName).getTextBox("accountPositionLabel").getText()));
  }

  public void toggleMainAccountGraphs(String text) {
    views.selectProjects();
    PopupButton toggle = new PopupButton(getPanel().getPanel("mainAccountsPanel").getButton("sectionTitleButton"));
    toggle.click(text);
  }

  public void toggleSavingsAccountGraphs(String text) {
    views.selectProjects();
    PopupButton toggle = new PopupButton(getPanel().getPanel("savingsAccountsPanel").getButton("sectionTitleButton"));
    toggle.click(text);
  }

  public HistoDailyChecker getMainSummaryGraph() {
    return getSummaryGraph("mainAccountsPanel", "account.summary.main");
  }

  public HistoDailyChecker getSavingsSummaryGraph() {
    return getSummaryGraph("savingsAccountsPanel", "account.summary.savings");
  }

  public HistoDailyChecker getSummaryGraph(String panelName, String titleKey) {
    Panel sectionPanel = getPanel().getPanel(panelName);
    List<String> result = new ArrayList<String>();
    for (UIComponent component : sectionPanel.getUIComponents(Button.class, "accountChartButton")) {
      result.add(component.getLabel());
    }
    TestUtils.assertEquals(result, Lang.get(titleKey));

    UISpecAssert.assertFalse(sectionPanel.getButton("accountChartButton").isVisible());

    return new HistoDailyChecker(sectionPanel, "accountHistoChart");
  }
}
