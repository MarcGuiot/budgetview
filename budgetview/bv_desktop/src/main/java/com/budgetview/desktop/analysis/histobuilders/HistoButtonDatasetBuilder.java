package com.budgetview.desktop.analysis.histobuilders;

import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.button.HistoButtonDataset;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;

public class HistoButtonDatasetBuilder extends HistoDatasetBuilder {

  private HistoButtonDataset dataset = new HistoButtonDataset("");

  public HistoButtonDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, HistoLabelUpdater labelUpdater) {
    super(histoChart, label, repository, labelUpdater);
  }

  public void addColumn(int monthId, boolean isSelectedMonth) {
    dataset.addColumn(monthId,
                      getLabel(monthId), getTooltipLabel(monthId), getSection(monthId),
                      isCurrentMonth(monthId), isFutureMonth(monthId), isSelectedMonth);
  }

  public void addButton(int minId, int maxId, String label, Key key, String tooltip, boolean selected, boolean enabled) {
    dataset.addButton(minId, maxId, label, key, tooltip, selected, enabled);
  }

  public HistoButtonDataset get() {
    return dataset;
  }
}
