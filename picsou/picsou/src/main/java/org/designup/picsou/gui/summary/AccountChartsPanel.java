package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.accounts.AccountViewPanel;
import org.designup.picsou.gui.accounts.actions.AccountPopupFactory;
import org.designup.picsou.gui.accounts.chart.AccountPositionsChartView;
import org.designup.picsou.gui.accounts.utils.AccountPositionStringifier;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AccountChartsPanel {
  private final AccountType accountType;
  private final HistoChartRange shortRange;
  private final HistoChartRange longRange;
  private final GlobRepository repository;
  private final Directory directory;
  private final ProjectChartView projects;
  private JPanel panel;

  public AccountChartsPanel(AccountType accountType, HistoChartRange shortRange, HistoChartRange longRange, GlobRepository repository, Directory directory, ProjectChartView projects) {
    this.accountType = accountType;
    this.shortRange = shortRange;
    this.longRange = longRange;
    this.repository = repository;
    this.directory = directory;
    this.projects = projects;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/accountChartsPanel.splits",
                                                      repository, directory);

    String sectionTitleKey = AccountType.MAIN.equals(accountType) ? "account.summary.main" : "account.summary.savings";
    builder.add("sectionTitle", new JLabel(Lang.get(sectionTitleKey)));

    builder.addRepeat("accountCharts", Account.TYPE,
                      Matchers.userCreatedAccounts(accountType),
                      new AccountComparator(),
                      new AccountRepeatFactory());

    panel = builder.load();
  }

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {

    public void registerComponents(PanelBuilder cellBuilder, Glob account) {

      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory);
      GlobButtonView accountButton = AccountViewPanel.createEditAccountButton(account, popupFactory, repository, directory);
      cellBuilder.add("accountChartButton", accountButton.getComponent());
      cellBuilder.addDisposable(accountButton);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .forceSelection(account.getKey());
      cellBuilder.add("accountPositionLabel", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      final AccountPositionsChartView accountChart =
        AccountPositionsChartView.full(account.get(Account.ID), "accountHistoChart", shortRange, repository, directory);
      final SplitsNode<HistoChart> chartNode = accountChart.registerComponents(cellBuilder);

      BooleanFieldListener showHide =
        BooleanFieldListener.installNodeStyle(account.getKey(), Account.SHOW_GRAPH,
                                              chartNode, "accountChartShown", "accountChartHidden", repository);
      cellBuilder.addDisposable(showHide);

      BooleanFieldListener listener =
        BooleanFieldListener.install(UserPreferences.KEY,
                                     UserPreferences.SHOW_PROJECT_DETAILS,
                                     repository, new BooleanListener() {
            public void apply(boolean showProjects) {
              HistoChartRange newRange = showProjects ? shortRange : longRange;
              projects.setRange(newRange);
              accountChart.setRange(newRange);
            }
          });
      cellBuilder.addDisposable(listener);
    }
  }

}
