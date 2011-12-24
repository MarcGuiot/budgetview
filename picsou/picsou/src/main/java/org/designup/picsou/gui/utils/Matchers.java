package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.*;

import static org.globsframework.model.utils.GlobMatchers.*;

public class Matchers {
  private Matchers() {
  }

  public static GlobMatcher transactionsForAllAccounts(GlobRepository repository) {
    return GlobMatchers.ALL;
  }

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
    accountIds = new HashSet<Integer>(accountIds);
    if (accountIds.contains(Account.ALL_SUMMARY_ACCOUNT_ID)) {
      return GlobMatchers.ALL;
    }
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

  public static GlobMatcher exportableTransactions() {
    return and(
      not(isTrue(Transaction.PLANNED)),
      not(isTrue(Transaction.MIRROR))
    );
  }

  public static MonthMatcher userSeriesActiveInPeriod() {
    return new SeriesFirstEndDateFilter(false) {
      protected boolean isEligible(Glob series, GlobRepository repository) {
        return !Utils.equal(series.get(Series.ID), Series.UNCATEGORIZED_SERIES_ID);
      }
    };
  }

  public static MonthMatcher seriesActiveInPeriod(final Integer budgetAreaId, boolean isExclusive) {
    return new SeriesFirstEndDateFilter(isExclusive) {
      protected boolean isEligible(Glob series, GlobRepository repository) {
        return budgetAreaId.equals(series.get(Series.BUDGET_AREA));
      }
    };
  }

  public static MonthMatcher seriesDateSavingsAndAccountFilter(final Integer accountId) {
    return new SeriesFirstEndDateFilter(false) {

      protected boolean isEligible(Glob series, GlobRepository repository) {
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
          return false;
        }
        return series.get(Series.TARGET_ACCOUNT).equals(accountId);
      }
    };
  }

  public static GlobMatcher deferredCardSeries() {
    return GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId());
  }

  public static CategorizationFilter deferredCardCategorizationFilter() {
    return seriesCategorizationFilter(BudgetArea.OTHER.getId());
  }

  public static CategorizationFilter seriesCategorizationFilter(final Integer budgetAreaId) {
    return new CategorizationFilter(budgetAreaId);
  }

  public static class CategorizationFilter implements GlobMatcher {
    private List<Glob> transactions = Collections.emptyList();
    private MonthMatcher filter;

    public CategorizationFilter(final Integer budgetAreaId) {
      filter = seriesActiveInPeriod(budgetAreaId, true);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (transactions.isEmpty()) {
        return false;
      }
      if (filter.matches(series, repository)) {
        if (series.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId()) &&
            series.get(Series.FROM_ACCOUNT) != null) {
          return checkInMain(repository);
        }
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
          return true;
        }
        Glob targetAccount = repository.findLinkTarget(series, Series.TARGET_ACCOUNT);
        for (Glob transaction : transactions) {
          if (!isSameAccount(repository, targetAccount, transaction)) {
            return false;
          }
          if (transaction.get(Transaction.AMOUNT) > 0 && !series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
            return false;
          }
          else if (transaction.get(Transaction.AMOUNT) < 0 && !series.get(Series.FROM_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    private boolean isSameAccount(GlobRepository repository, Glob account, Glob transaction) {
      if (Account.isMain(account)) {
        Glob operationAccount = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
        return AccountType.MAIN.getId().equals(operationAccount.get(Account.ACCOUNT_TYPE));
      }
      else if (Account.isSavings(account)) {
        return account.get(Account.ID).equals(transaction.get(Transaction.ACCOUNT));
      }
      return false;
    }

    public String toString() {
      return "CategorizationFilter(" + filter + ")";
    }

    private boolean checkInMain(GlobRepository repository) {
      for (Glob transaction : transactions) {
        Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
        if (!Account.isMain(account)) {
          return false;
        }
        if (!AccountCardType.NOT_A_CARD.getId().equals(account.get(Account.CARD_TYPE))) {
          return false;
        }
      }
      return true;
    }

    public void filterDates(Set<Integer> monthIds, List<Glob> transactions) {
      filter.filterMonths(monthIds);
      this.transactions = transactions;
    }
  }

  public static abstract class SeriesFirstEndDateFilter implements MonthMatcher {
    private boolean exclusive;
    private Set<Integer> monthIds = Collections.emptySet();

    private SeriesFirstEndDateFilter(boolean isExclusive) {
      this.exclusive = isExclusive;
    }

    public void filterMonths(Set<Integer> monthIds) {
      this.monthIds = monthIds;
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (!isEligible(series, repository)) {
        return false;
      }

      Integer firstMonth = series.get(Series.FIRST_MONTH);
      Integer lastMonth = series.get(Series.LAST_MONTH);
      if (firstMonth == null) {
        firstMonth = 0;
      }
      if (lastMonth == null) {
        lastMonth = Integer.MAX_VALUE;
      }
      for (Integer id : monthIds) {
        if ((id < firstMonth || id > lastMonth) == exclusive) {
          return !exclusive;
        }
        if (!exclusive) {
          Glob seriesBudget =
            repository
              .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
              .findByIndex(SeriesBudget.MONTH, id)
              .getGlobs()
              .getFirst();
          if (seriesBudget != null && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
            return true;
          }
        }
      }
      return exclusive;
    }

    public String toString() {
      return "SeriesFirstEndDateFilter(" + monthIds + "," + exclusive + ")";
    }

    protected abstract boolean isEligible(Glob series, GlobRepository repository);
  }

  public static GlobMatcher userCreatedSavingsAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedSavingsAccount(account);
      }
    };

  }

  public static class AccountDateMatcher implements GlobMatcher {
    private Set<Integer> months = new HashSet<Integer>();

    public AccountDateMatcher(GlobList months) {
      this.months.addAll(months.getValueSet(Month.ID));
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

  public static GlobMatcher unreconciled(final Set<Key> reconciledTransactions) {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        if (transaction == null) {
          return false;
        }
        Glob source = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
        if (source != null) {
          return !source.isTrue(Transaction.RECONCILED) || reconciledTransactions.contains(source.getKey());
        }
        return !transaction.isTrue(Transaction.RECONCILED) || reconciledTransactions.contains(transaction.getKey());
      }
    };
  }
}
