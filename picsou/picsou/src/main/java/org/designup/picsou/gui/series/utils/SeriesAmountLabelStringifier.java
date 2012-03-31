package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.description.stringifiers.MonthListStringifier;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

import java.util.SortedSet;

public class SeriesAmountLabelStringifier implements GlobListStringifier {
  private boolean autoSelectFutureMonths;

  public String toString(GlobList list, GlobRepository repository) {
    SortedSet<Integer> monthIds = list.getSortedSet(SeriesBudget.MONTH);

    if (monthIds.isEmpty()) {
      return Lang.get("seriesAmountEdition.period.short");
    }

    if (autoSelectFutureMonths) {
      Integer firstMonth = monthIds.first();
      return Lang.get("seriesAmountEdition.period.from", Month.getFullMonthLabelWith4DigitYear(firstMonth).toLowerCase());
    }

    String monthDescription = MonthListStringifier.toString(monthIds);
    return Lang.get("seriesAmountEdition.period.month", monthDescription.toLowerCase());
  }

  public void setAutoSelectFutureMonths(boolean autoSelectFutureMonths) {
    this.autoSelectFutureMonths = autoSelectFutureMonths;
  }
}
