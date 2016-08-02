package com.budgetview.desktop.analysis.histobuilders;

import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.daily.HistoDailyColors;
import com.budgetview.desktop.components.charts.histo.daily.HistoDailyDataset;
import com.budgetview.desktop.components.charts.histo.daily.HistoDailyPainter;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDailyDatasetBuilder extends HistoDatasetBuilder {
  private HistoDailyDataset dataset;

  public HistoDailyDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey,
                                  HistoLabelUpdater labelUpdater) {
    super(histoChart, label, repository, labelUpdater);
    Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
    Integer currentDayId = CurrentMonth.getLastTransactionDay(repository);
    this.dataset = new HistoDailyDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip",
                                         currentMonthId, currentDayId,
                                         getCurrentDayLabel(currentMonthId, currentDayId));
  }

  public void add(int monthId, Double[] values, boolean isSelectedMonth, boolean[] daySelections) {
    dataset.add(monthId, values, getLabel(monthId), getSection(monthId), isCurrentMonth(monthId), isSelectedMonth, daySelections);
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
