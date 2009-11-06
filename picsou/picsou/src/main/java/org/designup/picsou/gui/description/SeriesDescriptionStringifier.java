package org.designup.picsou.gui.description;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
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

  public static String getTransactionText(Glob transaction, GlobRepository repository) {
    return TRANSACTION_STRINGIFIER.toString(transaction, repository);
  }

  public static String toString(Glob series) {
    return Strings.toSplittedHtml(series.get(Series.DESCRIPTION), 50);
  }

}
