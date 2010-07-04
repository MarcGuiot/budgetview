package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffPainter;
import org.designup.picsou.model.CurrentMonth;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDiffDatasetBuilder extends HistoDatasetBuilder {

  private HistoDiffDataset dataset;
  private int multiplier = 1;
  private boolean showActualInTheFuture = true;
  private int lastMonthWithTransactions;

  HistoDiffDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.dataset  = new HistoDiffDataset("seriesEvolution.chart.histo." + tooltipKey + ".tooltip");
    this.lastMonthWithTransactions = CurrentMonth.getLastTransactionMonth(repository);
  }

  public void setActualHiddenInTheFuture() {
    showActualInTheFuture = false;
  }

  public void setInverted(boolean inverted) {
    this.multiplier = inverted ? -1 : 1;
  }

  public void add(int monthId, int currentMonthId, Double reference, Double actual) {
    dataset.add(monthId,
                reference != null ? reference * multiplier : 0,
                actual != null ? actual * multiplier : 0,
                getLabel(monthId), getTooltipLabel(monthId), getSection(monthId),
                monthId == currentMonthId,
                monthId > lastMonthWithTransactions);
  }

  public void addEmpty(int monthId, int currentMonthId) {
    add(monthId, currentMonthId, 0.0, 0.0);
  }

  public void apply(HistoDiffColors colors, String messageKey, String... args) {
    histoChart.update(new HistoDiffPainter(dataset, colors, showActualInTheFuture));
    updateHistoLabel(messageKey, args);
  }

  private void updateHistoLabel(String messageKey, String... args) {
    updateLabel(label, "chart.histo." + messageKey, args);
  }
}
