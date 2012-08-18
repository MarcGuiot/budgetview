package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.line.HistoBarPainter;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.designup.picsou.gui.components.charts.histo.line.HistoLinePainter;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class HistoLineDatasetBuilder extends HistoDatasetBuilder {
  private HistoLineDataset dataset;
  private boolean hasPositive;

  public HistoLineDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.dataset = new HistoLineDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip");
  }

  public void invertIfNeeded() {
    if (!hasPositive) {
      dataset.setInverted();
    }
  }

  public void add(int monthId, double value, boolean isSelectedMonth) {
    dataset.add(monthId, value,
                getLabel(monthId), getMonthLabel(monthId), getSection(monthId),
                isCurrentMonth(monthId), isFutureMonth(monthId), isSelectedMonth);
    hasPositive |= value > 0;
  }

  public void showBars(HistoLineColors colors, String messageKey, String... args) {
    histoChart.update(new HistoBarPainter(dataset, colors));
    updateLabel(label, "seriesAnalysis.chart.histo." + messageKey, args);
    updateLegend();
  }

  public void setKey(Key key) {
    setKeys(Collections.singleton(key));
  }

  public void setKeys(Set<Key> keys) {
    this.dataset.setKeys(keys);
  }
}
