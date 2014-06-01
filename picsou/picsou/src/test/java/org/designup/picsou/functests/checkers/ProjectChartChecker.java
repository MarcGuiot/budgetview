package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.HistoButtonChartChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.functests.checkers.components.PopupChecker;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
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
    checkComponentVisible(getHomePanel(), JButton.class, "createProject", true);
    checkComponentVisible(getHomePanel(), JPanel.class, "projectChart", false);
  }

  public void create() {
    getHomePanel().getButton("createProject").click();
  }

  public ConfirmationDialogChecker createAndOpenConfirmation() {
    return ConfirmationDialogChecker.open(getHomePanel().getButton("createProject").triggerClick());
  }

  public void checkShowDetailsButtonShown() {
    Button button = mainWindow.getPanel("summaryView").getButton("toggleProjectDetails");
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    assertThat(button.textEquals(Lang.get("summaryView.showProjectDetails")));
  }

  public void checkHideDetailsButtonShown() {
    views.selectHome();
    Button button = mainWindow.getPanel("summaryView").getButton("toggleProjectDetails");
    assertThat(button.isVisible());
    assertThat(button.isEnabled());
    assertThat(button.textEquals(Lang.get("summaryView.hideProjectDetails")));
  }

  public void checkToggleDetailsButtonHidden() {
    views.selectHome();
    Button button = mainWindow.getPanel("summaryView").getButton("toggleProjectDetails");
    assertFalse(button.isVisible());
    assertFalse(button.isEnabled());
  }

  public ProjectChartChecker showProjectDetails() {
    Button button = mainWindow.getPanel("summaryView").getButton("toggleProjectDetails");
    assertThat(button.textEquals(Lang.get("summaryView.showProjectDetails")));
    button.click();
    return this;
  }

  public ProjectChartChecker hideProjectDetails() {
    Button button = mainWindow.getPanel("summaryView").getButton("toggleProjectDetails");
    assertThat(button.textEquals(Lang.get("summaryView.hideProjectDetails")));
    button.click();
    return this;
  }

  public ProjectChartChecker checkRange(int firstMonth, int lastMonth) {
    getChart().checkRange(firstMonth, lastMonth);
    return this;
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
