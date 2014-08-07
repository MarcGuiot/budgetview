package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.HistoButtonChartChecker;
import org.designup.picsou.functests.checkers.components.PopupChecker;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.PopupMenuInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectChartChecker extends ViewChecker {

  private Panel homePanel;
  private HistoButtonChartChecker chart;

  public ProjectChartChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkShowsCreation() {
    checkComponentVisible(getProjectsPanel(), JButton.class, "createProject", true);
    checkComponentVisible(getProjectsPanel(), JPanel.class, "projectChart", false);
  }

  public void create() {
    getProjectsPanel().getButton("createProject").click();
  }

  public ConfirmationDialogChecker createAndOpenConfirmation() {
    return ConfirmationDialogChecker.open(getProjectsPanel().getButton("createProject").triggerClick());
  }

  public ProjectChartChecker checkRange(int firstMonth, int lastMonth) {
    getChart().checkRange(firstMonth, lastMonth);
    return this;
  }

  public void select(String projectName) {
    getChart().click(projectName, false);
  }

  public void selectMonth(int monthId) {
    getChart().clickColumnId(monthId);
  }

  public ProjectChartChecker checkNoProjectShown() {
    views.selectProjects();
    getChart().checkNoElementShown();
    return this;
  }

  public ProjectChartChecker checkProjectList(String... projectNames) {
    views.selectProjects();
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
      views.selectProjects();
      chart = new HistoButtonChartChecker(mainWindow, "summaryView", "projectChart");
    }
    return chart;
  }

  private Panel getProjectsPanel() {
    if (homePanel == null) {
      views.selectProjects();
      homePanel = mainWindow.getPanel("projectChartPanel");
    }
    return homePanel;
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
}
