package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyPainter;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDailyDatasetBuilder extends HistoDatasetBuilder {
  private HistoDailyDataset dataset;
  private boolean showFullMonthLabels;

  public HistoDailyDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey,
                                  boolean showFullMonthLabels) {
    super(histoChart, label, repository);
    this.showFullMonthLabels = showFullMonthLabels;
    this.dataset = new HistoDailyDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip",
                                         CurrentMonth.getLastTransactionMonth(repository),
                                         CurrentMonth.getLastTransactionDay(repository));
  }

  public void add(int monthId, Double[] values, boolean isSelectedMonth, boolean[] daySelections) {
    dataset.add(monthId, values, getLabel(monthId), getSection(monthId), isCurrentMonth(monthId), isSelectedMonth, daySelections);
  }

  protected String getLabel(int monthId) {
    return showFullMonthLabels ? Month.getFullMonthLabel(monthId) : Month.getShortMonthLabel(monthId);
  }

  public void apply(HistoDailyColors colors, String messageKey, String... args) {
    histoChart.update(new HistoDailyPainter(dataset, colors));
    updateLabel(label, "seriesAnalysis.chart.histo." + messageKey, args);
  }
}
