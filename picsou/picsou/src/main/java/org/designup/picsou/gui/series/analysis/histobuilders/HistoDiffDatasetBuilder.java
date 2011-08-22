package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffBarLinePainter;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffLegendPanel;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDiffDatasetBuilder extends HistoDatasetBuilder {

  private HistoDiffDataset dataset;
  private int lastMonthWithTransactions;
  private boolean hasPositive;
  private HistoDiffLegendPanel legend;

  HistoDiffDatasetBuilder(HistoChart histoChart, JLabel label, HistoDiffLegendPanel legend, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.legend = legend;
    this.dataset = new HistoDiffDataset("seriesAnalysis.chart.histo." + tooltipKey + ".tooltip");
    this.lastMonthWithTransactions = CurrentMonth.getLastTransactionMonth(repository);
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

    hasPositive |= adjustedReference > 0 || adjustedActual > 0;
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
    if (!hasPositive) {
      dataset.setInverted();
    }
  }

}
