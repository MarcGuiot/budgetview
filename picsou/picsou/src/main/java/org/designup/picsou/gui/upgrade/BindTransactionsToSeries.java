package org.designup.picsou.gui.upgrade;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import static org.globsframework.model.FieldValue.value;

public class BindTransactionsToSeries implements PostProcessor.Functor {

  private final Integer seriesId;
  private final GlobList transactions;

  public BindTransactionsToSeries(Glob series, GlobList transactions) {
    this.seriesId = series.get(Series.ID);
    this.transactions = transactions;
  }

  public void apply(GlobRepository repository) {
    run(transactions, seriesId, repository);
  }

  public static void run(GlobList transactions, Integer seriesId, GlobRepository repository) {
    for (Glob transaction : transactions) {
      if (transaction.exists()) {
        repository.update(transaction.getKey(),
                          value(Transaction.SERIES, seriesId),
                          value(Transaction.SUB_SERIES, null));
      }
    }
  }
}
