package org.designup.picsou.gui.transactions.utils;

import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class TransactionMatchers {
  public static GlobMatcher transactionsForMainAccounts(GlobRepository repository) {
    return transactionsForAccounts(Collections.singleton(Account.MAIN_SUMMARY_ACCOUNT_ID), repository);
  }

  public static GlobMatcher transactionsForSavingsAccounts(GlobRepository repository) {
    return transactionsForAccounts(Collections.singleton(Account.SAVINGS_SUMMARY_ACCOUNT_ID), repository);
  }

  public static GlobMatcher transactionsForAccount(Integer accountId) {
    return GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId);
  }

  public static GlobMatcher transactionsForAccounts(Set<Integer> accountIds, GlobRepository repository) {
    if (accountIds.contains(Account.ALL_SUMMARY_ACCOUNT_ID)) {
      return GlobMatchers.ALL;
    }

    accountIds = new HashSet<Integer>(accountIds);
    if (accountIds.contains(Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      accountIds.addAll(
        repository.getAll(Account.TYPE).filterSelf(
          not(fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId())), repository)
          .getValueSet(Account.ID));
    }
    if (accountIds.contains(Account.SAVINGS_SUMMARY_ACCOUNT_ID)) {
      accountIds.addAll(
        repository.getAll(Account.TYPE)
          .filterSelf(fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()), repository)
          .getValueSet(Account.ID));
    }
    return GlobMatchers.contained(Transaction.ACCOUNT, accountIds);
  }

  public static GlobMatcher transactionsForMonths(Set<Integer> months) {
    return GlobMatchers.contained(Transaction.BUDGET_MONTH, months);
  }

  public static GlobMatcher transactionsForBudgetAreas(final Set<Integer> budgetAreaIds) {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
        return (series != null) && budgetAreaIds.contains(series.get(Series.BUDGET_AREA));
      }

      public String toString() {
        return "transactions for BudgetAreas " + budgetAreaIds;
      }
    };
  }

  public static GlobMatcher transactionsForSeries(final Integer targetSeriesId) {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        Integer seriesId = transaction.get(Transaction.SERIES);
        return Utils.equal(targetSeriesId, seriesId);
      }

      public String toString() {
        return "transaction for series " + targetSeriesId;
      }
    };
  }

  public static GlobMatcher transactionsForSeries(final Set<Integer> targetSeries) {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        Integer seriesId = transaction.get(Transaction.SERIES);
        return targetSeries.contains(seriesId);
      }

      public String toString() {
        return "transaction for series " + targetSeries;
      }
    };
  }

  public static GlobMatcher uncategorizedTransactions() {
    return fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
  }

  public static GlobMatcher categorizedTransactions() {
    return and(
      isNotNull(Transaction.SERIES),
      not(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID)),
      isNotTrue(Transaction.PLANNED)
    );
  }

  public static GlobMatcher exportableTransactions() {
    return and(
      isNotTrue(Transaction.PLANNED),
      isNotTrue(Transaction.MIRROR)
    );
  }

  public static GlobMatcher transactionsToReconcile() {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        return transaction != null && transaction.isTrue(Transaction.TO_RECONCILE);
      }
    };
  }

  public static GlobMatcher missingReconciliationAnnotation(final Set<Key> reconciledTransactions) {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        if (transaction == null) {
          return false;
        }
        Glob source = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
        if (source != null) {
          return !source.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET) || reconciledTransactions.contains(source.getKey());
        }
        return !transaction.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET) || reconciledTransactions.contains(transaction.getKey());
      }
    };
  }

  public static GlobMatcher realTransactions() {
    return and(isNotTrue(Transaction.TO_RECONCILE),
               isFalse(Transaction.PLANNED),
               not(GlobMatchers.fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.OPEN_ACCOUNT_EVENT.getId())),
               not(GlobMatchers.fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.CLOSE_ACCOUNT_EVENT.getId())));
  }

  public static GlobMatcher realTransactions(int accountId) {
    return and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId),
               isNotTrue(Transaction.TO_RECONCILE),
               isFalse(Transaction.PLANNED),
               not(GlobMatchers.fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.OPEN_ACCOUNT_EVENT.getId())),
               not(GlobMatchers.fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.CLOSE_ACCOUNT_EVENT.getId())));
  }

  public static GlobMatcher realTransactions(int accountId, int monthId, int day) {
    return and(realTransactions(accountId),
               or(GlobMatchers.fieldStrictlyLessThan(Transaction.POSITION_MONTH, monthId),
                  and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                      fieldLessOrEqual(Transaction.POSITION_DAY, day))));
  }

  public static class AccountDateMatcher implements GlobMatcher {
    private Set<Integer> months = new HashSet<Integer>();

    public AccountDateMatcher(GlobList months) {
      this(months.getValueSet(Month.ID));
    }

    public AccountDateMatcher(Set<Integer> months) {
      this.months = months;
    }

    public boolean matches(Glob item, GlobRepository repository) {
      Date startDate = item.get(Account.OPEN_DATE);
      Date endDate = item.get(Account.CLOSED_DATE);
      if (startDate == null && endDate == null) {
        return true;
      }
      if (startDate != null && endDate != null) {
        int startMonthId = Month.getMonthId(startDate);
        int endMonthId = Month.getMonthId(endDate);
        for (Integer month : months) {
          if (month >= startMonthId && month <= endMonthId) {
            return true;
          }
        }
        return false;
      }
      if (startDate != null) {
        int startMonthId = Month.getMonthId(startDate);
        for (Integer month : months) {
          if (month >= startMonthId) {
            return true;
          }
        }
        return false;
      }
      // endDate != null
      int endMonthId = Month.getMonthId(endDate);
      for (Integer month : months) {
        if (month <= endMonthId) {
          return true;
        }
      }
      return false;
    }

    public String toString() {
      return "AccountDateMatcher" + months;
    }
  }
}
