package com.budgetview.gui.analysis.histobuilders;

import com.budgetview.gui.components.charts.histo.HistoChart;
import com.budgetview.model.Month;

public abstract class HistoLabelUpdater {
  public static HistoLabelUpdater get(HistoChart chart, int monthCount) {
    return get(chart, monthCount, false);
  }

  public static HistoLabelUpdater get(HistoChart chart, int monthCount, boolean showFullLabels) {
    if (chart == null) {
      return new OneLetter();
    }
    int maxLabelSize = chart.getMaxLabelSize(monthCount);
    if (showFullLabels && maxLabelSize > 8) {
      return new Full();
    }
    if (maxLabelSize >= 3) {
      return new Short();
    }
    return new OneLetter();
  }

  public abstract String getLabel(int monthId);

  private static class OneLetter extends HistoLabelUpdater {
    public String getLabel(int monthId) {
      return Month.getOneLetterMonthLabel(monthId);
    }
  }

  private static class Short extends HistoLabelUpdater {
    public String getLabel(int monthId) {
      return Month.getShortMonthLabel(monthId);
    }
  }

  private static class Full extends HistoLabelUpdater {
    public String getLabel(int monthId) {
      return Month.getFullMonthLabel(monthId, true);
    }
  }
}
