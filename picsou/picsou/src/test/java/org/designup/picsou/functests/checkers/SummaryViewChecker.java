package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class SummaryViewChecker extends ViewChecker {
  private Panel panel;
  private HistoDailyChecker mainChart;
  private HistoChecker savingsBalanceChart;
  private HistoChecker savingsChart;

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

  public HistoChecker getSavingsBalanceChart() {
    if  (savingsBalanceChart == null) {
      views.selectHome();
      savingsBalanceChart = new HistoChecker(mainWindow, "summaryView", "savingsBalanceHistoChart");
    }
    return savingsBalanceChart;
  }

  public HistoChecker getSavingsChart() {
    if  (savingsChart == null) {
      views.selectHome();
      savingsChart = new HistoChecker(mainWindow, "summaryView", "savingsHistoChart");
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
