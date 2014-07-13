package org.designup.picsou.gui.series.analysis;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.SelectNextMonthAction;
import org.designup.picsou.gui.actions.SelectPreviousMonthAction;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.printing.actions.PrintBudgetAction;
import org.designup.picsou.gui.series.analysis.components.SeriesAnalysisBreadcrumb;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.utils.SetFieldValueAction;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class SeriesAnalysisView extends View {

  private Directory parentDirectory;
  private PrintBudgetAction printBudgetAction;
  private SelectionService parentSelectionService;
  private SeriesChartsPanel charts;
  private Integer referenceMonthId;
  private JPanel chartsPanel;
  private JPanel tablePanel;
  private Map<AnalysisViewType, JCheckBoxMenuItem> viewSelectors = new HashMap<AnalysisViewType, JCheckBoxMenuItem>();

  public SeriesAnalysisView(GlobRepository repository, Directory directory, PrintBudgetAction printBudgetAction) {
    super(repository, createLocalDirectory(directory));
    this.parentDirectory = directory;
    this.printBudgetAction = printBudgetAction;
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
          charts.monthSelected(referenceMonthId, monthIds);
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
        for (Glob group : selection.getAll(SeriesGroup.TYPE)) {
          builder.add(SeriesWrapper.getWrapperForSeriesGroup(group.get(SeriesGroup.ID), repository));
        }
        selectionService.select(builder.get());
      }
    }, Series.TYPE, SubSeries.TYPE, SeriesGroup.TYPE);

    SeriesAnalysisBreadcrumb breadcrumb = new SeriesAnalysisBreadcrumb(repository, directory);
    builder.add("breadcrumb", breadcrumb.getEditor());

    this.charts = new SeriesChartsPanel(new ScrollableHistoChartRange(12, 6, false, repository), repository, directory, parentSelectionService);
    this.charts.registerCharts(builder);
    this.chartsPanel = new JPanel();
    builder.add("chartsPanel", chartsPanel);

    this.tablePanel = new JPanel();
    builder.add("tablePanel", tablePanel);

    JPopupMenu actionsMenu = new JPopupMenu();
    for (AnalysisViewType view : AnalysisViewType.values()) {
      JCheckBoxMenuItem menuItem = createMenuItem(view);
      viewSelectors.put(view, menuItem);
      actionsMenu.add(menuItem);
    }
    repository.addChangeListener(new KeyChangeListener(UserPreferences.KEY) {
      public void update() {
        updateViewType();
      }
    });
    actionsMenu.addSeparator();
    actionsMenu.add(printBudgetAction);
    builder.add("actionsMenu", new JPopupButton(Lang.get("actions"), actionsMenu));

    SeriesEvolutionTableView tableView = new SeriesEvolutionTableView(repository, seriesChartsColors,
                                                                      directory, parentDirectory);
    tableView.registerComponents(builder);

    builder.add("previousMonth", new SelectPreviousMonthAction(repository, parentDirectory));
    builder.add("nextMonth", new SelectNextMonthAction(repository, parentDirectory));

    builder.add("analysisTableSplit", SplitPaneConfig.create(directory, LayoutConfig.ANALYSIS_TABLE));

    return builder;
  }

  private JCheckBoxMenuItem createMenuItem(final AnalysisViewType viewType) {
    return new JCheckBoxMenuItem(new SetFieldValueAction(viewType.getLabel(),
                                                    UserPreferences.KEY,
                                                    UserPreferences.ANALYSIS_VIEW_TYPE,
                                                    viewType.getId(),
                                                    repository));
  }

  public void reset() {
    charts.reset();
    referenceMonthId = null;
    updateViewType();
  }

  private void updateViewType() {
    AnalysisViewType currentType = AnalysisViewType.get(repository);
    switch (currentType) {
      case CHARTS:
        chartsPanel.setVisible(true);
        tablePanel.setVisible(false);
        break;
      case TABLE:
        chartsPanel.setVisible(false);
        tablePanel.setVisible(true);
        break;
      case BOTH:
        chartsPanel.setVisible(true);
        tablePanel.setVisible(true);
        break;
    }
    for (Map.Entry<AnalysisViewType, JCheckBoxMenuItem> entry : viewSelectors.entrySet()) {
      entry.getValue().setSelected(entry.getKey().equals(currentType));
    }
  }
}

