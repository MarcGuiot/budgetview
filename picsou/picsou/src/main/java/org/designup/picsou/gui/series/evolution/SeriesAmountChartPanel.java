package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartUpdater;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SeriesAmountChartPanel {

  private HistoChart chart;
  private Integer currentSeriesId;
  private HistoChartUpdater updater;
  private Integer selectedMonthId;

  public SeriesAmountChartPanel(GlobRepository repository, Directory directory) {

    HistoChartBuilder histoChartBuilder = new HistoChartBuilder(true, true, true, false, repository, directory,
                                                                directory.get(SelectionService.class), 4, 12);
    histoChartBuilder.addListener(new HistoChartListener() {
      public void columnsClicked(Set<Integer> ids) {
      }

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
