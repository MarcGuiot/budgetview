package org.designup.picsou.gui.series.analysis;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.SelectNextMonthAction;
import org.designup.picsou.gui.actions.SelectPreviousMonthAction;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.series.analysis.components.SeriesAnalysisBreadcrumb;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.model.LayoutConfig;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.ToggleVisibilityAction;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.SortedSet;

public class SeriesAnalysisView extends View {

  private Directory parentDirectory;
  private SelectionService parentSelectionService;
  private SeriesChartsPanel chartPanel;
  private Integer referenceMonthId;
  private ToggleVisibilityAction tableToggleAction;

  public SeriesAnalysisView(GlobRepository repository, Directory directory) {
    super(repository, createLocalDirectory(directory));
    this.parentDirectory = directory;
    this.parentSelectionService = directory.get(SelectionService.class);
  }

  private static Directory createLocalDirectory(Directory parentDirectory) {
    Directory localDirectory = new DefaultDirectory(parentDirectory);
    SelectionService localSelectionService = new SelectionService();
    localDirectory.add(localSelectionService);
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    parentBuilder.add("seriesAnalysisView", createLocalPanel());
  }

  private GlobsPanelBuilder createLocalPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/analysis/seriesAnalysisView.splits",
                                                      repository, directory);

    SeriesChartsColors seriesChartsColors = new SeriesChartsColors(repository, directory);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        SortedSet<Integer> monthIds = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
        if (!monthIds.isEmpty()) {
          referenceMonthId = monthIds.iterator().next();
          chartPanel.monthSelected(referenceMonthId, monthIds);
        }
      }
    }, Month.TYPE);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobSelectionBuilder builder = GlobSelectionBuilder.init();
        for (Glob series : selection.getAll(Series.TYPE)) {
          builder.add(SeriesWrapper.getWrapperForSeries(series.get(Series.ID), repository));
        }
        for (Glob subSeries : selection.getAll(SubSeries.TYPE)) {
          builder.add(SeriesWrapper.getWrapperForSubSeries(subSeries.get(SubSeries.ID), repository));
        }
        selectionService.select(builder.get());
      }
    }, Series.TYPE, SubSeries.TYPE);

    SeriesAnalysisBreadcrumb breadcrumb = new SeriesAnalysisBreadcrumb(repository, directory);
    builder.add("breadcrumb", breadcrumb.getEditor());

    this.chartPanel = new SeriesChartsPanel(new ScrollableHistoChartRange(12, 6, false, repository), repository, directory, parentSelectionService);
    this.chartPanel.registerCharts(builder);

    JPanel tablePanel = new JPanel();
    builder.add("tablePanel", tablePanel);
    tableToggleAction = new ToggleVisibilityAction(tablePanel,
                                                   Lang.get("seriesAnalysis.table.toggle.shown"),
                                                   Lang.get("seriesAnalysis.table.toggle.hidden"));
    builder.add("toggleTable", tableToggleAction);

    SeriesEvolutionTableView tableView = new SeriesEvolutionTableView(repository, seriesChartsColors,
                                                                      directory, parentDirectory);
    tableView.registerComponents(builder);

    builder.add("previousMonth", new SelectPreviousMonthAction(repository, parentDirectory));
    builder.add("nextMonth", new SelectNextMonthAction(repository, parentDirectory));

    builder.add("analysisTableSplit", SplitPaneConfig.create(directory, LayoutConfig.ANALYSIS_TABLE));

    return builder;
  }

  public void reset() {
    chartPanel.reset();
    referenceMonthId = null;
    tableToggleAction.setHidden();
  }

}

