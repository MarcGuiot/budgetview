package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyPainter;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.model.CurrentMonth;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDailyDatasetBuilder extends HistoDatasetBuilder {
  private HistoDailyDataset dataset;

  public HistoDailyDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.dataset = new HistoDailyDataset("seriesEvolution.chart.histo." + tooltipKey + ".tooltip",
                                         CurrentMonth.getLastTransactionMonth(repository),
                                         CurrentMonth.getLastTransactionDay(repository));
  }

  public void add(int monthId, Double[] values, boolean isSelectedMonth) {
    dataset.add(monthId, values, getLabel(monthId), getMonthLabel(monthId), getSection(monthId), isCurrentMonth(monthId), isSelectedMonth);
  }

  public void apply(HistoDailyColors colors, String messageKey, String... args) {
    histoChart.update(new HistoDailyPainter(dataset, colors));
    updateLabel(label, "seriesEvolution.chart.histo." + messageKey, args);
  }
}
