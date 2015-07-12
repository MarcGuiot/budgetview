package org.designup.picsou.gui.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffBarLinePainter;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffLegendPanel;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class HistoDiffDatasetBuilder extends HistoDatasetBuilder {

  private HistoDiffDataset dataset;
  private int lastMonthWithTransactions;
  private int positiveCount = 0;
  private int negativeCount = 0;
  private HistoDiffLegendPanel legend;

  HistoDiffDatasetBuilder(HistoChart histoChart, JLabel label, HistoDiffLegendPanel legend, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.legend = legend;
    this.dataset = new HistoDiffDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip");
    this.lastMonthWithTransactions = CurrentMonth.getLastTransactionMonth(repository);
  }

  public void setKey(Key key) {
    this.dataset.setKeys(Collections.singleton(key));
  }

  public void setKeys(Set<Key> keys) {
    this.dataset.setKeys(keys);
  }

  public void add(int monthId, Double reference, Double actual, boolean isSelectedMonth) {
    double adjustedReference = reference != null ? reference : 0.00;
    double adjustedActual = actual != null ? actual : 0.00;
    this.dataset.add(monthId,
                     adjustedReference,
                     adjustedActual,
                     getLabel(monthId), getMonthLabel(monthId), getSection(monthId),
                     isCurrentMonth(monthId), isSelectedMonth,
                     monthId > lastMonthWithTransactions);

    if (adjustedReference > 0 || adjustedActual > 0) {
      positiveCount++;
    }
    else if (adjustedReference < 0 || adjustedActual < 0) {
      negativeCount++;
    }

  }

  public void addEmpty(int monthId, boolean isSelectedMonth) {
    add(monthId, 0.0, 0.0, isSelectedMonth);
  }

  public void showDiff(HistoDiffColors colors, String fillTextKey, String lineTextKey,
                       String messageKey, String... args) {
    complete();
    HistoDiffBarLinePainter painter = new HistoDiffBarLinePainter(dataset, colors, false);
    histoChart.update(painter);

    updateLabel(label, "seriesAnalysis.chart.histo." + messageKey, args);

    legend.show(Lang.get(lineTextKey), Lang.get(fillTextKey));
  }

  private void complete() {
    if (negativeCount + positiveCount == 0) {
      return;
    }
    double ratio = (double)positiveCount / (double)(positiveCount + negativeCount);
    if (ratio < 0.3) {
      dataset.setInverted();
    }
  }
}
