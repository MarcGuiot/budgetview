package com.budgetview.desktop.analysis;

import com.budgetview.desktop.analysis.histobuilders.HistoChartBuilder;
import com.budgetview.desktop.analysis.histobuilders.HistoChartUpdater;
import com.budgetview.desktop.analysis.histobuilders.range.ScrollableHistoChartRange;
import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.HistoChartColors;
import com.budgetview.desktop.components.charts.histo.utils.HistoChartListenerAdapter;
import com.budgetview.model.SeriesBudget;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SeriesAmountChartPanel {

  private HistoChart chart;
  private Integer currentSeriesId;
  private HistoChartUpdater updater;
  private Integer selectedMonthId;

  public SeriesAmountChartPanel(GlobRepository repository, Directory directory) {

    HistoChartConfig config = new HistoChartConfig(true, true, false, true, true, true, false, true, true, false);
    config.setUseWheelScroll(true);
    final HistoChartBuilder histoChartBuilder =
      new HistoChartBuilder(config,
                            new HistoChartColors("histo", directory), new ScrollableHistoChartRange(4, 12, true, repository),
                            repository, directory, directory.get(SelectionService.class));
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void scroll(int count) {
        updater.update(false);
      }
    });
    histoChartBuilder.setSnapToScale(true);
    updater = new HistoChartUpdater(repository, directory,
                                    SeriesBudget.TYPE,
                                    SeriesBudget.MONTH,
                                    SeriesBudget.TYPE) {
      protected void update(Integer monthId, boolean resetPosition) {
        if ((currentSeriesId != null)  && (selectedMonthId != null)) {
          histoChartBuilder.showSeriesBudget(currentSeriesId,
                                             selectedMonthId,
                                             currentMonths, resetPosition);
        }
      }
    };

    chart = histoChartBuilder.getChart();
  }

  public void init(Integer seriesId, Integer currentMonthId) {
    this.currentSeriesId = seriesId;
    this.selectedMonthId = currentMonthId;
    this.updater.update(true);
  }

  public HistoChart getChart() {
    return chart;
  }
}
