package org.designup.picsou.gui.series.analysis;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.SelectNextMonthAction;
import org.designup.picsou.gui.actions.SelectPreviousMonthAction;
import org.designup.picsou.gui.series.analysis.components.SeriesAnalysisBreadcrumb;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.ToggleVisibilityAction;
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
  private SeriesChartsColors seriesChartsColors;

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

    seriesChartsColors = new SeriesChartsColors(repository, directory);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        SortedSet<Integer> monthIds = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
        if (!monthIds.isEmpty()) {
          referenceMonthId = monthIds.iterator().next();
          chartPanel.monthSelected(referenceMonthId, monthIds);
        }
      }
    }, Month.TYPE);

    SeriesAnalysisBreadcrumb breadcrumb = new SeriesAnalysisBreadcrumb(repository, directory);
    builder.add("breadcrumb", breadcrumb.getEditor());

    this.chartPanel = new SeriesChartsPanel(new ScrollableHistoChartRange(12, 6, false, repository), repository, directory, parentSelectionService);
    this.chartPanel.registerCharts(builder);

    JPanel tablePanel = new JPanel();
    builder.add("tablePanel", tablePanel);
    tablePanel.setVisible(false);
    builder.add("toggleTable",
                new ToggleVisibilityAction(tablePanel,
                                           Lang.get("seriesAnalysis.table.toggle.shown"),
                                           Lang.get("seriesAnalysis.table.toggle.hidden")));

    SeriesEvolutionTableView tableView = new SeriesEvolutionTableView(repository, seriesChartsColors,
                                                                      directory, parentDirectory);
    tableView.registerComponents(builder);

    builder.add("expand", tableView.getExpandAction());
    builder.add("collapse", tableView.getCollapseAction());
    builder.add("previousMonth", new SelectPreviousMonthAction(repository, parentDirectory));
    builder.add("nextMonth", new SelectNextMonthAction(repository, parentDirectory));

    return builder;
  }

  public void reset() {
    chartPanel.reset();
    referenceMonthId = null;
  }

}

