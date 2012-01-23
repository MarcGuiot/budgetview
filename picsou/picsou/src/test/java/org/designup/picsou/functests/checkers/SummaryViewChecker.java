package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.HistoChartChecker;
import org.designup.picsou.functests.checkers.components.HistoDailyChecker;
import org.uispec4j.Panel;
import org.uispec4j.Window;

public class SummaryViewChecker extends ViewChecker {
  private Panel panel;
  private HistoDailyChecker mainChart;
  private HistoChartChecker savingsChart;

  public SummaryViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public HistoDailyChecker getMainChart() {
    if  (mainChart == null) {
      views.selectHome();
      mainChart = new HistoDailyChecker(getPanel(), "mainAccountsHistoChart");
    }
    return mainChart;
  }

  public HistoChartChecker getSavingsChart() {
    if  (savingsChart == null) {
      views.selectHome();
      savingsChart = new HistoChartChecker(mainWindow, "summaryView", "savingsHistoChart");
    }
    return savingsChart;
  }

  public SummaryViewChecker checkTuningHelp(String title) {
    HelpChecker.open(getPanel().getButton("openTuningHelp").triggerClick()).checkTitle(title).close();
    return this;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("summaryView");
    }
    return panel;
  }
}
