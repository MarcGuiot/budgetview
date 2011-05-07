package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffBarLinePainter;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffSummaryPainter;
import org.designup.picsou.model.CurrentMonth;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDiffDatasetBuilder extends HistoDatasetBuilder {

  private HistoDiffDataset dataset;
  private int multiplier = 1;
  private int lastMonthWithTransactions;

  HistoDiffDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.dataset = new HistoDiffDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip");
    this.lastMonthWithTransactions = CurrentMonth.getLastTransactionMonth(repository);
  }

  public void setInverted(boolean inverted) {
    this.multiplier = inverted ? -1 : 1;
  }

  public void add(int monthId, Double reference, Double actual, boolean isSelectedMonth) {
    dataset.add(monthId,
                reference != null ? reference * multiplier : 0,
                actual != null ? actual * multiplier : 0,
                getLabel(monthId), getMonthLabel(monthId), getSection(monthId),
                isCurrentMonth(monthId), isSelectedMonth,
                monthId > lastMonthWithTransactions);
  }

  public void addEmpty(int monthId, boolean isSelectedMonth) {
    add(monthId, 0.0, 0.0, isSelectedMonth);
  }

  public void showBarLine(HistoDiffColors colors, String messageKey, String... args) {
    HistoDiffBarLinePainter painter = new HistoDiffBarLinePainter(dataset, colors, false);
    apply(painter, messageKey, args);
  }

  public void showSummary(HistoDiffColors colors, boolean showReference, String messageKey, String... args) {
    HistoDiffSummaryPainter painter = new HistoDiffSummaryPainter(dataset, showReference, colors);
    apply(painter, messageKey, args);
  }

  private void apply(HistoPainter painter, String messageKey, String[] args) {
    histoChart.update(painter);
    updateHistoLabel(messageKey, args);
  }

  private void updateHistoLabel(String messageKey, String... args) {
    updateLabel(label, "seriesAnalysis.chart.histo." + messageKey, args);
  }
}
