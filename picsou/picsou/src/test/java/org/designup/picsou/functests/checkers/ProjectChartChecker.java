package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.HistoButtonChartChecker;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectChartChecker extends ViewChecker {

  private Panel homePanel;
  private HistoButtonChartChecker chart;

  public ProjectChartChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkShowsCreation() {
    checkComponentVisible(getHomePanel(), JButton.class, "createProject", true);
    checkComponentVisible(getHomePanel(), JPanel.class, "projectChart", false);
  }

  public void create() {
    getHomePanel().getButton("createProject").click();
  }

  public void checkShowDetailsButtonShown() {
    Button button = mainWindow.getPanel("summaryView").getButton("showProjectDetails");
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
  }

  public void checkShowDetailsButtonHidden() {
    views.selectHome();
    Button button = mainWindow.getPanel("summaryView").getButton("showProjectDetails");
    UISpecAssert.assertFalse(button.isEnabled());
  }

  public void checkShowsChart() {
    checkComponentVisible(getHomePanel(), JPanel.class, "projectChart", true);
  }

  public void select(String projectName) {
    getChart().click(projectName, false);
  }

  public void selectMonth(int monthId) {
    getChart().clickColumnId(monthId);
  }

  public ProjectChartChecker checkNoProjectShown() {
    getChart().checkNoElementShown();
    return this;
  }

  public ProjectChartChecker checkProjectList(String... projectNames) {
    getChart().checkElementNames(projectNames);
    return this;
  }

  public ProjectChartChecker checkProject(String projectName, int start, int end, double amount) {
    getChart().checkElementPeriod(projectName, start, end);
    getChart().checkElementTooltipContains(projectName, "Planned: " + toString(amount));
    return this;
  }

  public HistoButtonChartChecker getChart() {
    if (chart == null) {
      views.selectHome();
      chart = new HistoButtonChartChecker(mainWindow, "home", "projectChart");
    }
    return chart;
  }

  private Panel getHomePanel() {
    if (homePanel == null) {
      views.selectHome();
      homePanel = mainWindow.getPanel("projectChartPanel");
    }
    return homePanel;
  }
}
