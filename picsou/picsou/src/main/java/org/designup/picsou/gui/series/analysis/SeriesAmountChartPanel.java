package org.designup.picsou.gui.series.analysis;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartUpdater;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SeriesAmountChartPanel {

  private HistoChart chart;
  private Integer currentSeriesId;
  private HistoChartUpdater updater;
  private Integer selectedMonthId;

  public SeriesAmountChartPanel(GlobRepository repository, Directory directory) {

    HistoChartBuilder histoChartBuilder =
      new HistoChartBuilder(new HistoChartConfig(true, true, false, true),
                            new HistoChartRange(4, 12, true, repository),
                            repository, directory, directory.get(SelectionService.class));
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void scroll(int count) {
        updater.update(false);
      }
    });
    histoChartBuilder.setSnapToScale(true);
    updater = new HistoChartUpdater(histoChartBuilder, repository, directory,
                                    SeriesBudget.TYPE, SeriesBudget.MONTH,
                                    SeriesBudget.TYPE) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer monthId, boolean resetPosition) {
        if (currentSeriesId != null) {
          histoChartBuilder.showSeriesBudget(currentSeriesId,
                                             SeriesAmountChartPanel.this.selectedMonthId,
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
