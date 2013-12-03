package org.designup.picsou.gui.summary;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsChartView;
import org.designup.picsou.gui.help.actions.HelpAction;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.SetBooleanAction;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
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

    final MainDailyPositionsChartView mainDailyPositions =
      new MainDailyPositionsChartView(shortRange,
                                      getMainAccountsChartConfig(),
                                      "mainAccountsHistoChart",
                                      repository, directory, "daily");
    mainDailyPositions.registerComponents(builder);
    builder.add("openTuningHelp", new HelpAction(Lang.get("summaryView.openTuningHelp.text"),
                                                 "tuning", "", directory));

    final SavingsAccountsChartView savingsAccounts =
      new SavingsAccountsChartView(shortRange,
                                   getSavingsAccountsChartConfig(),
                                   repository, directory);
    savingsAccounts.registerComponents(builder);

    BooleanFieldListener.install(UserPreferences.KEY,
                                 UserPreferences.SHOW_PROJECT_DETAILS,
                                 repository, new BooleanListener() {
      public void apply(boolean showProjects) {
        HistoChartRange newRange = showProjects ? shortRange : longRange;
        projects.setRange(newRange);
        mainDailyPositions.setRange(newRange);
        savingsAccounts.setRange(newRange);
      }
    });
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE, Project.TYPE) {
      protected void update(GlobRepository repository) {
        updateShowProjectDetails(repository, showProjectDetails);
      }
    });

    parentBuilder.add("summaryView", builder);

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

  private void updateShowProjectDetails(GlobRepository repository, JButton showProjectDetails) {
    boolean show = repository.contains(Project.TYPE);
    Glob prefs = repository.find(UserPreferences.KEY);
    if (prefs != null && prefs.isTrue(UserPreferences.SHOW_PROJECT_DETAILS)) {
      show = false;
    }
    showProjectDetails.setEnabled(show);
    GuiUtils.revalidate(showProjectDetails);
  }

  public static HistoChartConfig getMainAccountsChartConfig() {
    return new HistoChartConfig(true, false, true, true, true, true, true, true, true, true);
  }

  public static HistoChartConfig getSavingsAccountsChartConfig() {
    return new HistoChartConfig(false, false, true, true, true, true, false, true, true, true);
  }
}
