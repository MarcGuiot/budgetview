package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsBalanceChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsChartView;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartRange;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SummaryView extends View {

  public static final int MONTHS_BACK = 3;
  public static final int MONTHS_FORWARD = 9;

  public SummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/summaryView.splits",
                                                      repository, directory);

    HistoChartRange range = new HistoChartRange(MONTHS_BACK, MONTHS_FORWARD, false, repository);

    MainDailyPositionsChartView mainDailyPositions = new MainDailyPositionsChartView(range, repository, directory);
    mainDailyPositions.registerComponents(builder);

    SavingsAccountsChartView savingsAccounts = new SavingsAccountsChartView(range, repository, directory);
    savingsAccounts.registerComponents(builder);

    SavingsAccountsBalanceChartView savingsAccountsBalance = new SavingsAccountsBalanceChartView(range, repository, directory);
    savingsAccountsBalance.registerComponents(builder);

    parentBuilder.add("summaryView", builder);
  }
}
