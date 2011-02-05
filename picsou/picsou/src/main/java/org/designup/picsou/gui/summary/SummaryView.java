package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsBalanceChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsChartView;
import org.designup.picsou.gui.components.charts.histo.utils.ScrollGroup;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SummaryView extends View {

  public SummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/summaryView.splits",
                                                      repository, directory);

    ScrollGroup group = new ScrollGroup();

    MainDailyPositionsChartView mainDailyPositionsView = new MainDailyPositionsChartView(repository, directory);
    mainDailyPositionsView.registerComponents(builder);
    mainDailyPositionsView.register(group);

    SavingsAccountsChartView savingsAccountsView = new SavingsAccountsChartView(repository, directory);
    savingsAccountsView.registerComponents(builder);
    savingsAccountsView.register(group);

    SavingsAccountsBalanceChartView savingsAccountsBalanceView = new SavingsAccountsBalanceChartView(repository, directory);
    savingsAccountsBalanceView.registerComponents(builder);
    savingsAccountsBalanceView.register(group);

    parentBuilder.add("summaryView", builder);
  }
}
