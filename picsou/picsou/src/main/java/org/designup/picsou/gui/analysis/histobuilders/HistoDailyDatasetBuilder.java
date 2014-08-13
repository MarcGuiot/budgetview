package org.designup.picsou.gui.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyPainter;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDailyDatasetBuilder extends HistoDatasetBuilder {
  private HistoDailyDataset dataset;
  private boolean showFullMonthLabels;

  public HistoDailyDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey,
                                  boolean showFullMonthLabels) {
    super(histoChart, label, repository);
    this.showFullMonthLabels = showFullMonthLabels;
    Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
    Integer currentDayId = CurrentMonth.getLastTransactionDay(repository);
    this.dataset = new HistoDailyDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip",
                                         currentMonthId, currentDayId,
                                         getCurrentDayLabel(currentMonthId, currentDayId));
  }

  public void add(int monthId, Double[] values, boolean isSelectedMonth, boolean[] daySelections) {
    dataset.add(monthId, values, getLabel(monthId), getSection(monthId), isCurrentMonth(monthId), isSelectedMonth, daySelections);
  }

  protected String getLabel(int monthId) {
    return showFullMonthLabels ? Month.getFullMonthLabel(monthId) : Month.getShortMonthLabel(monthId);
  }

  private String getCurrentDayLabel(Integer currentMonthId, Integer currentDayId) {
    if (currentMonthId == 0) {
      return "";
    }
    return Lang.get("seriesAnalysis.chart.histo.currentDayLabel", Month.getShortMonthLabel(currentMonthId), currentDayId);
  }

  public void apply(HistoDailyColors colors, String messageKey, String... args) {
    histoChart.update(new HistoDailyPainter(dataset, colors));
    updateLabel(label, "seriesAnalysis.chart.histo." + messageKey, args);
    updateLegend();
  }
}
