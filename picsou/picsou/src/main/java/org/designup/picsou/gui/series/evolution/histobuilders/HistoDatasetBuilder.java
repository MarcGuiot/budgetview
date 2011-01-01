package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class HistoDatasetBuilder {

  protected final HistoChart histoChart;
  protected final JLabel label;
  protected final GlobRepository repository;

  public HistoDatasetBuilder(HistoChart histoChart, JLabel label, GlobRepository repository) {
    this.histoChart = histoChart;
    this.label = label;
    this.repository = repository;
  }

  protected String getLabel(int monthId) {
    return Month.getOneLetterMonthLabel(monthId);
  }

  protected String getMonthLabel(int monthId) {
    return Month.getFullMonthLabelWith4DigitYear(monthId);
  }

  protected String getSection(int monthId) {
    return Integer.toString(Month.toYear(monthId));
  }

  protected void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get("seriesEvolution." + messageKey, args));
  }
}
