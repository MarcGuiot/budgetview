package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.painters.*;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoLineDatasetBuilder extends HistoDatasetBuilder {
  private HistoLineDataset dataset;

  public HistoLineDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, String tooltipKey) {
    super(histoChart, label, repository);
    this.dataset  = new HistoLineDataset(tooltipKey);
  }
  
  public void add(int monthId, double value, boolean isSelectedMonth) {
    dataset.add(monthId, value, getLabel(monthId), getTooltipLabel(monthId), getSection(monthId), isSelectedMonth);    
  }

  public void apply(HistoLineColors colors, String messageKey, String... args) {
    histoChart.update(new HistoLinePainter(dataset, colors));
    updateLabel(label, "chart.histo." + messageKey, args);
  }

}
