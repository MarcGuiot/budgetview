package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.components.charts.histo.button.HistoButtonDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectViewChecker extends ViewChecker {

  private Panel panel;
  private HistoButtonChartChecker chart;

  public ProjectViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public ProjectEditionChecker create() {
    return ProjectEditionChecker.open(getPanel().getButton("createProject"));
  }

  public ProjectEditionChecker edit(String projectName) {
    return ProjectEditionChecker.open(getChart().triggerClick(projectName));
  }

  public ProjectViewChecker checkNoProjectShown() {
    getChart().checkNoElementShown();
    return this;
  }

  public ProjectViewChecker checkProjectList(String... projectNames) {
    getChart().checkElementNames(projectNames);
    return this;
  }

  public ProjectViewChecker checkProject(String projectName, int start, int end, double amount) {
    getChart().checkElementPeriod(projectName, start, end);
    getChart().checkElementTooltipContains(projectName, "Planned: " + toString(amount));
    return this;
  }

  public void checkHintMessageDisplayed() {
    checkComponentVisible(getPanel(), JEditorPane.class, "projectHint", true);
  }

  public void checkHintMessageHidden() {
    checkComponentVisible(getPanel(), JEditorPane.class, "projectHint", false);
  }

  private HistoButtonChartChecker getChart() {
    if (chart == null) {
      init();
    }
    return chart;
  }

  private Panel getPanel() {
    if (panel == null) {
      init();
    }
    return panel;
  }

  private void init() {
    views.selectHome();
    panel = mainWindow.getPanel("home");
    chart = new HistoButtonChartChecker(mainWindow, "home", "projectChart");
    chart.init();
  }
}
