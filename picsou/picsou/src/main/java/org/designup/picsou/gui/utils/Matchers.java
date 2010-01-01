package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;

import java.util.*;

public class Matchers {
  private Matchers() {
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

  public static GlobMatcher transactionsForSeries(final Set<Integer> targetBudgetAreas,
                                                  Set<Integer> targetSeries,
                                                  GlobRepository repository) {
    if (targetBudgetAreas.contains(BudgetArea.ALL.getId())) {
      return GlobMatchers.ALL;
    }
    final Set<Integer> reducedSeriesSet = new HashSet<Integer>();
    for (Integer seriesId : targetSeries) {
      Glob series = repository.find(Key.create(Series.TYPE, seriesId));
      if (series != null && !targetBudgetAreas.contains(series.get(Series.BUDGET_AREA))) {
        reducedSeriesSet.add(seriesId);
      }
    }
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        Integer seriesId = transaction.get(Transaction.SERIES);
        if (reducedSeriesSet.contains(seriesId)) {
          return true;
        }
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        return targetBudgetAreas.contains(series.get(Series.BUDGET_AREA));
      }
    };
  }

  public static GlobMatcher exportableTransactions() {
    return and(
      not(isTrue(Transaction.PLANNED)),
      not(isTrue(Transaction.MIRROR))
    );
  }

  public static SeriesFirstEndDateFilter seriesDateFilter(final Integer budgetAreaId, boolean isExclusive) {
    return new SeriesFirstEndDateFilter(isExclusive) {

      protected boolean isEligible(Glob series, GlobRepository repository) {
        return budgetAreaId.equals(series.get(Series.BUDGET_AREA));
      }
    };
  }

  public static SeriesFirstEndDateFilter seriesDateSavingsAndAccountFilter(final Integer accountId) {
    return new SeriesFirstEndDateFilter(false) {

      protected boolean isEligible(Glob series, GlobRepository repository) {
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
          return false;
        }
        Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
        Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
        if (Account.areBothImported(toAccount, fromAccount)) {
          if (accountId == Account.MAIN_SUMMARY_ACCOUNT_ID) {
            if (fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
              return series.isTrue(Series.IS_MIRROR);
            }
            if (toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
              return !series.isTrue(Series.IS_MIRROR);
            }
            return false;
          }
          if (accountId == Account.SAVINGS_SUMMARY_ACCOUNT_ID) {
            if (fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
              return series.isTrue(Series.IS_MIRROR);
            }
            if (toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
              return !series.isTrue(Series.IS_MIRROR);
            }
            return false;
          }
          if (accountId.equals(fromAccount.get(Account.ID))) {
            return series.isTrue(Series.IS_MIRROR);
          }
          if (accountId.equals(toAccount.get(Account.ID))) {
            return !series.isTrue(Series.IS_MIRROR);
          }
        }
        if (accountId == Account.MAIN_SUMMARY_ACCOUNT_ID) {
          if (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
            return true;
          }
          if (fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
            return true;
          }
        }
        if (accountId == Account.SAVINGS_SUMMARY_ACCOUNT_ID) {
          if (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
            return true;
          }
          if (fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
            return true;
          }
        }

        return (accountId.equals(series.get(Series.FROM_ACCOUNT)) || accountId.equals(series.get(Series.TO_ACCOUNT)));
      }
    };
  }

  public static GlobMatcher deferredCardSeries() {
    // TODO: à completer
    return GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId());
  }

  public static CategorizationFilter deferredCardCategorizationFilter() {
    // TODO: à completer
    return seriesCategorizationFilter(BudgetArea.OTHER.getId());
  }

  public static CategorizationFilter seriesCategorizationFilter(final Integer budgetAreaId) {
    return new CategorizationFilter(budgetAreaId);
  }

  public static class CategorizationFilter implements GlobMatcher {
    private List<Glob> transactions = Collections.emptyList();
    private SeriesFirstEndDateFilter filter;

    public CategorizationFilter(final Integer budgetAreaId) {
      filter = seriesDateFilter(budgetAreaId, true);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (transactions.isEmpty()){
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
        Integer toAccountId = series.get(Series.TO_ACCOUNT);
        Integer fromAccountId = series.get(Series.FROM_ACCOUNT);
        Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
        Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
        if (Account.onlyOneIsImported(toAccount, fromAccount)) {
          for (Glob transaction : transactions) {
            if (transaction.get(Transaction.AMOUNT) > 0) {
              if (!toAccount.isTrue(Account.IS_IMPORTED_ACCOUNT)) {
                return false;
              }
            }
            else {
              if (!fromAccount.isTrue(Account.IS_IMPORTED_ACCOUNT)) {
                return false;
              }
            }
          }
        }
        else if (toAccountId != null && fromAccountId != null) {
          for (Glob transaction : transactions) {
            if (transaction.get(Transaction.AMOUNT) > 0) {
              if (series.isTrue(Series.IS_MIRROR)) {
                return false;
              }
              if (!toAccountId.equals(transaction.get(Transaction.ACCOUNT))) {
                return false;
              }
            }
            else {
              if (!series.isTrue(Series.IS_MIRROR)) {
                return false;
              }
              if (!fromAccountId.equals(transaction.get(Transaction.ACCOUNT))) {
                return false;
              }
            }
          }
        }
        else if (toAccountId != null || fromAccountId != null) {
          for (Glob transaction : transactions) {
            if (transaction.get(Transaction.AMOUNT) < 0) {
              if (fromAccountId != null) {
                if (!fromAccountId.equals(transaction.get(Transaction.ACCOUNT))) {
                  return false;
                }
              }
              else {
                return false;
              }
            }
            else {
              if (toAccountId != null) {
                if (!toAccountId.equals(transaction.get(Transaction.ACCOUNT))) {
                  return false;
                }
              }
              else {
                return false;
              }
            }
          }
        }
        return true;
      }
      return false;
    }

    private boolean checkInMain(GlobRepository repository) {
      for (Glob transaction : transactions) {
        Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
        if (!account.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
          return false;
        }
        if (!account.get(Account.CARD_TYPE).equals(AccountCardType.NOT_A_CARD.getId())) {
          return false;
        }
      }
      return true;
    }

    public void filterDates(Set<Integer> monthIds, List<Glob> transactions) {
      filter.filterDates(monthIds);
      this.transactions = transactions;
    }
  }

  static public abstract class SeriesFirstEndDateFilter implements GlobMatcher {
    private boolean exclusive;
    private Set<Integer> monthIds = Collections.emptySet();

    private SeriesFirstEndDateFilter(boolean isExclusive) {
      exclusive = isExclusive;
    }

    public void filterDates(Set<Integer> monthIds) {
      this.monthIds = monthIds;
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (isEligible(series, repository)) {
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
            Glob seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
              .findByIndex(SeriesBudget.MONTH, id).getGlobs().getFirst();
            if (seriesBudget != null && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
              return true;
            }
          }
        }
        return exclusive;
      }
      return false;
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
  }

  public static void main(String[] args) {
    int id = 6;
    boolean ex = false;
    if ((id < 5 || id > 10) == ex) {
      System.out.println("PicsouMatchers.main " + !ex);
    }
  }
}
