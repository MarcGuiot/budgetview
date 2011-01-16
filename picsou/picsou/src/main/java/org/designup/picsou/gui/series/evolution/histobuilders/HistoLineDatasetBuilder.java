package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.line.HistoBarPainter;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.designup.picsou.gui.components.charts.histo.line.HistoLinePainter;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoLineDatasetBuilder extends HistoDatasetBuilder {
  private HistoLineDataset dataset;

  public HistoLineDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.dataset = new HistoLineDataset("seriesEvolution.chart.histo." + tooltipKey + ".tooltip");
  }

  public void add(int monthId, double value, boolean isSelectedMonth) {
    dataset.add(monthId, value,
                getLabel(monthId), getMonthLabel(monthId), getSection(monthId),
                isCurrentMonth(monthId), isFutureMonth(monthId), isSelectedMonth);
  }

  public void showLine(HistoLineColors colors, String messageKey, String... args) {
    histoChart.update(new HistoLinePainter(dataset, colors));
    updateLabel(label, "seriesEvolution.chart.histo." + messageKey, args);
  }

  public void showBars(HistoLineColors colors, String messageKey, String... args) {
    histoChart.update(new HistoBarPainter(dataset, colors));
    updateLabel(label, "seriesEvolution.chart.histo." + messageKey, args);
  }
}
