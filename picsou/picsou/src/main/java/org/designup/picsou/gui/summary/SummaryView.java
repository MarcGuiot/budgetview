package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.AccountViewPanel;
import org.designup.picsou.gui.accounts.actions.AccountPopupFactory;
import org.designup.picsou.gui.accounts.chart.AccountPositionsChartView;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.SetBooleanAction;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class SummaryView extends View {

  public SummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/summaryView.splits",
                                                      repository, directory);

    final HistoChartRange shortRange = new ScrollableHistoChartRange(2, 7, false, repository);
    final HistoChartRange longRange = new ScrollableHistoChartRange(6, 12, false, repository);

    final ProjectChartView projects = new ProjectChartView(shortRange, repository, directory);
    projects.registerComponents(builder);

    final JButton showProjectDetails =
      new JButton(new SetBooleanAction(UserPreferences.KEY,
                                       UserPreferences.SHOW_PROJECT_DETAILS,
                                       true,
                                       Lang.get("summaryView.showProjectDetails"),
                                       repository));
    builder.add("showProjectDetails", showProjectDetails);

    final SplitsNode<JLabel> projectArrow = builder.add("projectArrow", new JLabel());

    builder.addRepeat("accountCharts", Account.TYPE,
                      Matchers.userCreatedAccounts(),
                      new AccountComparator(),
                      new AccountRepeatFactory(shortRange, longRange, projects));
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE, Project.TYPE) {
      public void update(GlobRepository repository) {
        updateShowProjectDetails(repository, showProjectDetails);
      }
    });

    parentBuilder.add("summaryView", builder);

    parentBuilder.add("summaryProjectSplit", SplitPaneConfig.create(directory, LayoutConfig.HOME_SUMMARY_PROJECTS));

    parentBuilder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        BooleanFieldListener.installNodeStyle(UserPreferences.KEY,
                                              UserPreferences.SHOW_PROJECT_DETAILS,
                                              projectArrow, "arrowShown", "arrowHidden",
                                              repository);
        updateShowProjectDetails(repository, showProjectDetails);
      }
    });
  }

  private void updateShowProjectDetails(GlobRepository repository, final JButton showProjectDetails) {
    boolean show = repository.contains(Project.TYPE);
    Glob prefs = repository.find(UserPreferences.KEY);
    if (prefs != null && prefs.isTrue(UserPreferences.SHOW_PROJECT_DETAILS)) {
      show = false;
    }
    showProjectDetails.setEnabled(show);
  }

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {
    private final HistoChartRange shortRange;
    private final HistoChartRange longRange;
    private final ProjectChartView projects;

    public AccountRepeatFactory(HistoChartRange shortRange, HistoChartRange longRange, ProjectChartView projects) {
      this.shortRange = shortRange;
      this.longRange = longRange;
      this.projects = projects;
    }

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

    private class AccountPositionStringifier implements GlobListStringifier {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return "";
        }
        Glob account = list.get(0);
        return Lang.get("summaryView.account.position",
                        Formatting.toString(account.get(Account.POSITION_WITH_PENDING)),
                        AccountPositionLabels.getAccountPositionDate(account));
      }
    }
  }
}
