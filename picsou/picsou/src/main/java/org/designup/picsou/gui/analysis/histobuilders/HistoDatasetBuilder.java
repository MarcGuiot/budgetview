package org.designup.picsou.gui.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDatasetBuilder {

  protected final HistoChart histoChart;
  protected final JLabel label;
  protected final GlobRepository repository;
  private HistoLabelUpdater labelUpdater;

  public HistoDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository, HistoLabelUpdater labelUpdater) {
    this.histoChart = histoChart;
    this.label = label;
    this.repository = repository;
    this.labelUpdater = labelUpdater;
  }

  protected String getLabel(int monthId) {
    return labelUpdater.getLabel(monthId);
  }

  protected String getTooltipLabel(int monthId) {
    return Month.getFullMonthLabelWith4DigitYear(monthId, true);
  }

  protected String getSection(int monthId) {
    return Integer.toString(Month.toYear(monthId));
  }

  protected void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get(messageKey, args));
  }

  protected void updateLegend() {
  }

  protected boolean isCurrentMonth(int monthId) {
    return CurrentMonth.isCurrentMonth(monthId, repository);
  }

  protected boolean isFutureMonth(int monthId) {
    return monthId > CurrentMonth.getCurrentMonth(repository);
  }
}
