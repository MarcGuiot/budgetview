package org.designup.picsou.gui.analysis.evolution;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.analysis.SeriesChartsColors;
import org.designup.picsou.gui.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.analysis.utils.AnalysisViewPanel;
import org.designup.picsou.gui.budget.SeriesEditionButtons;
import org.designup.picsou.gui.budget.components.NameLabelPopupButton;
import org.designup.picsou.gui.components.charts.histo.HistoChartColors;
import org.designup.picsou.gui.series.utils.SeriesOrGroup;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperComparator;
import org.designup.picsou.gui.series.view.SeriesWrapperMatchers;
import org.designup.picsou.gui.series.view.SeriesWrapperStringifier;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobSelectablePanel;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.*;

public class EvolutionAnalysisView extends AnalysisViewPanel {

  private static final int MONTHS_FORWARD = 6;
  private final SeriesWrapperComparator comparator;
  private final GlobMatcher seriesMatcher;
  private Integer referenceMonthId;
  private GlobRepeat seriesRepeat;
  private BudgetArea currentBudgetArea = BudgetArea.RECURRING;
  private List<SeriesChart> seriesCharts = new ArrayList<SeriesChart>();
  private ScrollableHistoChartRange range;
  private HistoChartConfig histoChartConfig;
  private HistoChartBuilder histoChartBuilder;

  private static int MONTHS_BACK = 36;
  private SelectionService selectionService;

  public EvolutionAnalysisView(String name, GlobRepository repository,
                               Directory parentDirectory,
                               Directory directory, SeriesChartsColors seriesChartsColors) {
    super(name, "/layout/analysis/evolutionAnalysisView.splits",
          repository, parentDirectory, directory, seriesChartsColors);
    this.selectionService = directory.get(SelectionService.class);
    this.comparator = new SeriesWrapperComparator(repository, repository, new SeriesWrapperStringifier(repository, directory));
    final SeriesWrapperMatchers.ActiveSeries activeSeriesMatcher = new SeriesWrapperMatchers.ActiveSeries() {
      public Iterable<Integer> getMonthRange() {
        return Month.range(Month.offset(referenceMonthId, -MONTHS_BACK),
                           Month.offset(referenceMonthId, MONTHS_FORWARD));
      }

      public Integer getReferenceMonthId() {
        return referenceMonthId;
      }
    };
    this.seriesMatcher = new GlobMatcher() {
      public boolean matches(Glob wrapper, GlobRepository repository) {
        if (!SeriesWrapper.isSeries(wrapper)) {
          return false;
        }
        Glob series = SeriesWrapper.getSeries(wrapper, repository);
        if (!currentBudgetArea.equals(Series.getBudgetArea(series))) {
          return false;
        }
        return activeSeriesMatcher.matches(wrapper, repository);
      }
    };

    this.range = new ScrollableHistoChartRange(MONTHS_BACK, MONTHS_FORWARD, false, repository);
    this.histoChartConfig = new HistoChartConfig(true, true, false, true, true, false, false, true, true, false);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    builder.addRepeat("budgetAreas", Arrays.asList(BudgetArea.INCOME_AND_EXPENSES_AREAS), new BudgetAreaFactory());

    histoChartBuilder =
      new HistoChartBuilder(histoChartConfig,
                            new HistoChartColors("histo", directory), range, repository, directory, parentDirectory.get(SelectionService.class));
    builder.addDisposable(histoChartBuilder);
    builder.add("budgetAreasChart", histoChartBuilder.getChart());
    builder.add("histoChartLabel", histoChartBuilder.getLabel());
    builder.add("histoChartLegend", histoChartBuilder.getLegend());

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(currentBudgetArea, repository, parentDirectory);
    seriesRepeat = GlobsPanelBuilder.addRepeat("seriesRepeat", SeriesWrapper.TYPE, seriesMatcher, comparator,
                                               repository, builder, new SeriesFactory(seriesButtons));

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList budgetAreas = selection.getAll(BudgetArea.TYPE);
        if (!budgetAreas.isEmpty()) {
          currentBudgetArea = BudgetArea.get(budgetAreas.getFirst().get(BudgetArea.ID));
        }
        else {
          currentBudgetArea = null;
        }
        update(true);
      }
    }, BudgetArea.TYPE);

    update(false);
  }

  public void reset() {
    selectionService.select(repository.get(BudgetArea.INCOME.getKey()));
    update(true);
  }

  public void update(boolean clearBeforeRefresh) {
    Gui.setWaitCursor(directory.get(JFrame.class));
    if (clearBeforeRefresh) {
      seriesRepeat.clear();
    }
    if ((referenceMonthId == null) || repository.find(CurrentMonth.KEY) == null) {
      histoChartBuilder.clear();
    }
    else {
      histoChartBuilder.showBudgetAreaHisto(Collections.singleton(currentBudgetArea), referenceMonthId, false);
    }
    GuiUtils.runLater(new Runnable() {
      public void run() {
        seriesRepeat.refresh();
        Gui.setDefaultCursor(directory.get(JFrame.class));
      }
    });
  }

  public void monthSelected(Integer referenceMonthId, SortedSet<Integer> monthIds) {
    this.referenceMonthId = referenceMonthId;
    update(false);
  }

  private class BudgetAreaFactory implements RepeatComponentFactory<BudgetArea> {
    public void registerComponents(PanelBuilder cellBuilder, BudgetArea budgetArea) {
      SplitsNode<JPanel> selectionPanel = cellBuilder.add("selectionPanel", new JPanel());
      final GlobSelectablePanel selectablePanel =
        new GlobSelectablePanel(selectionPanel,
                                "selectedPanel", "unselectedPanel",
                                "selectedRolloverPanel", "unselectedRolloverPanel",
                                repository, directory, budgetArea.getKey());
      selectablePanel.setMultiSelectionEnabled(false);
      selectablePanel.setUnselectEnabled(false);
      cellBuilder.addDisposable(selectablePanel);

      cellBuilder.add("budgetArea", new JLabel(budgetArea.getLabel()));
    }
  }

  private class SeriesFactory implements RepeatComponentFactory<Glob> {
    private static final int DEFAULT_SERIES_CHART_HEIGHT = 115;
    private SeriesEditionButtons seriesButtons;

    public SeriesFactory(SeriesEditionButtons seriesButtons) {
      this.seriesButtons = seriesButtons;
    }

    public void registerComponents(PanelBuilder cellBuilder, Glob seriesWrapper) {
      Glob series = SeriesWrapper.getSeries(seriesWrapper, repository);
      NameLabelPopupButton button = seriesButtons.createSeriesPopupButton(series);
      cellBuilder.add("seriesButton", button.getComponent());
      cellBuilder.addDisposable(button);

      final HistoChartBuilder seriesChartBuilder =
        new HistoChartBuilder(histoChartConfig,
                              new HistoChartColors("histo", directory), range, repository, directory, parentDirectory.get(SelectionService.class));
      cellBuilder.addDisposable(seriesChartBuilder);
      cellBuilder.add("seriesChart", seriesChartBuilder.getChart());

      final SeriesChart chart = new SeriesChart(series, seriesChartBuilder);
      seriesCharts.add(chart);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          seriesCharts.remove(chart);
        }
      });
      cellBuilder.addDisposable(chart);

      GuiUtils.runLater(new Runnable() {
        public void run() {
          chart.update();
        }
      });
    }
  }

  private class SeriesChart implements Disposable {
    private HistoChartBuilder histoChartBuilder;
    private Set<SeriesOrGroup> selectedSeriesOrGroups = new HashSet<SeriesOrGroup>();

    public SeriesChart(Glob series, HistoChartBuilder builder) {
      this.selectedSeriesOrGroups.add(new SeriesOrGroup(series));
      this.histoChartBuilder = builder;
    }

    public void update() {
      if (histoChartBuilder == null) {
        return;
      }
      if ((referenceMonthId == null) || repository.find(CurrentMonth.KEY) == null) {
        histoChartBuilder.clear();
        return;
      }
      histoChartBuilder.showSeriesHisto(selectedSeriesOrGroups, referenceMonthId, false);
    }

    public void dispose() {
      histoChartBuilder.dispose();
      histoChartBuilder = null;
      selectedSeriesOrGroups.clear();
    }
  }
}
