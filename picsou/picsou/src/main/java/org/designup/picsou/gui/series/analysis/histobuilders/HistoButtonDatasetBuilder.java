package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.button.HistoButtonDataset;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;

public class HistoButtonDatasetBuilder extends HistoDatasetBuilder {

  private HistoButtonDataset dataset = new HistoButtonDataset("");

  public HistoButtonDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository) {
    super(histoChart, label, repository);
  }

  public void addColumn(int monthId, boolean isSelectedMonth) {
    dataset.addColumn(monthId,
                      getLabel(monthId), getMonthLabel(monthId), getSection(monthId),
                      isCurrentMonth(monthId), isFutureMonth(monthId), isSelectedMonth);
  }

  public void addButton(int minId, int maxId, String label, Key key, String tooltip) {
    dataset.addButton(minId, maxId, label, key, tooltip);
  }

  public HistoButtonDataset get() {
    return dataset;
  }
}
