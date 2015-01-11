package org.designup.picsou.gui.categorization.utils;

import org.designup.picsou.gui.transactions.utils.TransactionMatchers;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class Uncategorized {
  public static int getCount(Set<Integer> selectedAccounts, GlobRepository repository) {
    return repository.getAll(Transaction.TYPE, TransactionMatchers.uncategorizedForAccounts(selectedAccounts)).size();
  }

  public static double getAbsAmount(Integer monthId, GlobRepository repository) {
    double uncategorizedAbs = 0;
    for (Glob transaction : getTransactions(monthId, repository)) {
      uncategorizedAbs += Math.abs(transaction.get(Transaction.AMOUNT));
    }
    return uncategorizedAbs;
  }

  private static GlobList getTransactions(Integer monthId, GlobRepository repository) {
    ReadOnlyGlobRepository.MultiFieldIndexed index = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
    GlobList prefiltered = index.findByIndex(Transaction.POSITION_MONTH, Month.previous(monthId)).getGlobs();
    prefiltered.addAll(index.findByIndex(Transaction.POSITION_MONTH, monthId).getGlobs());
    prefiltered.addAll(index.findByIndex(Transaction.POSITION_MONTH, Month.next(monthId)).getGlobs());
    return prefiltered
      .filterSelf(fieldEquals(Transaction.MONTH, monthId), repository)
      .filterSelf(fieldEquals(Transaction.PLANNED, false), repository);
  }

  public static GlobList getTransactions(Set<Integer> selectedMonthIds, GlobRepository repository) {
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                                 TransactionMatchers.userCreated(),
                                 fieldIn(Transaction.MONTH, selectedMonthIds)));
  }

  public static GlobList getTransactions(GlobRepository repository) {
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                                 TransactionMatchers.userCreated()));
  }

  public static Level getLevel(Set<Integer> selectedMonths, boolean filterOnCurrentMonth, GlobRepository repository) {
    GlobMatcher monthFilter =
      filterOnCurrentMonth ? GlobMatchers.fieldIn(Transaction.MONTH, selectedMonths) : GlobMatchers.ALL;
    GlobList transactions =
      repository.getAll(Transaction.TYPE, and(TransactionMatchers.realTransactions(), monthFilter));

//    GlobList transactions =
//      filterOnCurrentMonth ? getTransactions(selectedMonths, repository) : getTransactions(repository);

    double total = 0;
    double uncategorized = 0;
    for (Glob transaction : transactions) {
      double amount = Math.abs(transaction.get(Transaction.AMOUNT));
      total += amount;
      if (Transaction.isUncategorized(transaction)) {
        uncategorized += amount;
      }
    }
    return new Level(total, uncategorized);
  }

  public static class Level {
    public final double total;
    public final double uncategorized;

    public Level(double total, double uncategorized) {
      this.total = total;
      this.uncategorized = uncategorized;
    }
  }
}
