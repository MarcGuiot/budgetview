package org.designup.picsou.gui.series.utils;

import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.utils.Lang;

import java.util.Set;

public class SeriesAmountLabelStringifier implements GlobListStringifier {
  public String toString(GlobList list, GlobRepository repository) {
    Set<Integer> monthIds = list.getValueSet(SeriesBudget.MONTH);
    String monthDescription = MonthListStringifier.toString(monthIds);
    if (Strings.isNullOrEmpty(monthDescription)) {
      return Lang.get("seriesBudgetEdition.amount.label.short");
    }
    else {
      return Lang.get("seriesBudgetEdition.amount.label.full", monthDescription.toLowerCase());
    }
  }
}
