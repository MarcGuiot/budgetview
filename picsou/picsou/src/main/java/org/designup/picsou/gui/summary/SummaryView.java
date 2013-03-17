package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsChartView;
import org.designup.picsou.gui.card.ImportPanel;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.help.actions.HelpAction;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.utils.Lang;
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

    ImportPanel importPanel = new ImportPanel(ImportPanel.Mode.COMPACT, false, repository, directory);
    importPanel.registerComponents(builder);

    HistoChartRange range = new ScrollableHistoChartRange(MONTHS_BACK, MONTHS_FORWARD, false, repository);

    ProjectChartView projects = new ProjectChartView(range, repository, directory);
    projects.registerComponents(builder);

    MainDailyPositionsChartView mainDailyPositions =
      new MainDailyPositionsChartView(range,
                                      getChartConfigWithoutLabels(true),
                                      "mainAccountsHistoChart",
                                      repository, directory, "daily");
    mainDailyPositions.registerComponents(builder);
    builder.add("openTuningHelp", new HelpAction(Lang.get("summaryView.openTuningHelp.text"),
                                                 "tuning", "", directory));

    SavingsAccountsChartView savingsAccounts =
      new SavingsAccountsChartView(range,
                                   getChartConfigWithoutLabels(false),
                                   repository, directory);
    savingsAccounts.registerComponents(builder);

    parentBuilder.add("summaryView", builder);
  }

  public static HistoChartConfig getChartConfigWithoutLabels(boolean drawInnerAnnotations) {
    return new HistoChartConfig(false, false, false, true, true, true, true, true, drawInnerAnnotations);
  }
}
