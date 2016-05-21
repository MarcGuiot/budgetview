package com.budgetview.gui.description.stringifiers;

import com.budgetview.model.Transaction;
import com.budgetview.model.Series;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.Strings;

public class SeriesDescriptionStringifier extends AbstractGlobStringifier {
  private static final GlobStringifier TRANSACTION_STRINGIFIER = GlobStringifiers.target(Transaction.SERIES, new SeriesDescriptionStringifier());

  public static GlobStringifier transactionSeries() {
    return TRANSACTION_STRINGIFIER;
  }

  public String toString(Glob series, GlobRepository repository) {
    if (series == null) {
      return "";
    }

    return toString(series);
  }

  public static String toString(Glob series) {
    return Strings.toSplittedHtml(series.get(Series.DESCRIPTION), 50);
  }
}
