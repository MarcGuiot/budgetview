package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainAccountsChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsChartView;
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
    MainAccountsChartView mainAccountsView = new MainAccountsChartView(repository, directory);
    mainAccountsView.registerComponents(builder);

    SavingsAccountsChartView savingsAccountsView = new SavingsAccountsChartView(repository, directory);
    savingsAccountsView.registerComponents(builder);

    parentBuilder.add("summaryView", builder);

  }
}
