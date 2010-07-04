package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartUpdater;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SeriesAmountChartPanel {

  private HistoChart chart;
  private Integer currentSeriesId;
  private HistoChartUpdater updater;
  private Integer currentMonthId;

  public SeriesAmountChartPanel(GlobRepository repository, Directory directory) {

    HistoChartBuilder histoChartBuilder = new HistoChartBuilder(true, true, repository, directory, directory.get(SelectionService.class), 4, 12);
    updater = new HistoChartUpdater(histoChartBuilder, repository, directory,
                                    SeriesBudget.TYPE, SeriesBudget.MONTH,
                                    SeriesBudget.TYPE) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer monthId) {
        if (currentSeriesId != null) {
          histoChartBuilder.showSeriesBudget(currentSeriesId, currentMonthId);
        }
      }
    };

    chart = histoChartBuilder.getChart();
  }

  public void init(Integer seriesId, Integer monthId) {
    this.currentSeriesId = seriesId;
    this.updater.update();
    this.currentMonthId = monthId;
  }

  public HistoChart getChart() {
    return chart;
  }
}
