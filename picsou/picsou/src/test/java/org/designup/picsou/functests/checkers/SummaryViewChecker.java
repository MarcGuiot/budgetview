package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class SummaryViewChecker extends ViewChecker {
  private Panel panel;
  private HistoChecker mainChart;
  private HistoChecker savingsChart;

  public SummaryViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public HistoChecker getMainChart() {
    if  (mainChart == null) {
      views.selectHome();
      mainChart = new HistoChecker(mainWindow, "summaryView", "mainAccountsHistoChart");
    }
    return mainChart;
  }
  
  public HistoChecker getSavingsChart() {
    if  (savingsChart == null) {
      views.selectHome();
      savingsChart = new HistoChecker(mainWindow, "summaryView", "savingsHistoChart");
    }
    return savingsChart;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("summaryView");
    }
    return panel;
  }
}
