package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import com.budgetview.shared.utils.Amounts;
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
      not(isTrue(Transaction.PLANNED))
    );
  }

  public static GlobMatcher exportableTransactions() {
    return and(
      not(isTrue(Transaction.PLANNED)),
      not(isTrue(Transaction.MIRROR))
    );
  }

  public static GlobMatcher transactionsToReconcile() {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        return transaction.isTrue(Transaction.TO_RECONCILE);
      }
    };
  }

  public static MonthMatcher seriesActiveInPeriod(final Integer budgetAreaId, boolean showOnlyForActiveMonths, boolean showOnlyIfAvailableOnAllMonths) {
    return new SeriesFirstEndDateFilter(showOnlyForActiveMonths, showOnlyIfAvailableOnAllMonths) {
      protected boolean isEligible(Glob series, GlobRepository repository) {
        return budgetAreaId.equals(series.get(Series.BUDGET_AREA));
      }
    };
  }

  public static MonthMatcher seriesDateSavingsAndAccountFilter(final Integer accountId) {
    return new SeriesFirstEndDateFilter(true, false) {
      protected boolean isEligible(Glob series, GlobRepository repository) {
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
          return false;
        }
        return series.get(Series.TARGET_ACCOUNT).equals(accountId);
      }
    };
  }

  public static GlobMatcher deferredCardSeries() {
    return GlobMatchers.and(GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                            GlobMatchers.not(GlobMatchers.fieldEquals(Series.ID, Series.ACCOUNT_SERIES_ID)));
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
      filter = seriesActiveInPeriod(budgetAreaId, false, true);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (transactions.isEmpty()) {
        return false;
      }
      if (series.get(Series.ID).equals(Series.ACCOUNT_SERIES_ID)){
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
    private boolean showOnlyForActiveMonths;
    private boolean showOnlyIfAvailableOnAllMonths;
    private Set<Integer> selectedMonthIds = Collections.emptySet();
    private Set<Integer> expandedMonthIds = Collections.emptySet();

    private SeriesFirstEndDateFilter(boolean showOnlyForActiveMonths, boolean showOnlyIfAvailableOnAllMonths) {
      this.showOnlyForActiveMonths = showOnlyForActiveMonths;
      this.showOnlyIfAvailableOnAllMonths = showOnlyIfAvailableOnAllMonths;
    }

    public void filterMonths(Set<Integer> monthIds) {
      this.selectedMonthIds = monthIds;

      this.expandedMonthIds = new HashSet<Integer>();
      for (Integer monthId : monthIds) {
        expandedMonthIds.add(Month.previous(monthId));
        expandedMonthIds.add(Month.next(monthId));
      }
      expandedMonthIds.removeAll(selectedMonthIds);
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

      boolean monthsInScope = isMonthSelectionInSeriesScope(firstMonth, lastMonth);

      for (Integer monthId : selectedMonthIds) {
        Glob seriesBudget = SeriesBudget.find(series.get(Series.ID), monthId, repository);
        if ((seriesBudget != null)) {
          if (Amounts.isNotZero(seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT))) {
            return true;
          }
          if (monthsInScope && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
            return true;
          }
        }
      }

      if (monthsInScope) {
        for (Integer monthId : expandedMonthIds) {
          Glob seriesBudget = SeriesBudget.find(series.get(Series.ID), monthId, repository);
          if ((seriesBudget != null) && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
            return true;
          }
        }
      }

      return false;
    }

    private boolean isMonthSelectionInSeriesScope(Integer firstMonth, Integer lastMonth) {
      boolean inScope;
      if (showOnlyIfAvailableOnAllMonths) {
        inScope = true;
        for (Integer id : selectedMonthIds) {
          if ((id < firstMonth) || (id > lastMonth)) {
            inScope = false;
            break;
          }
        }
      }
      else {
        inScope = false;
        for (Integer id : selectedMonthIds) {
          if ((id >= firstMonth) && (id <= lastMonth)) {
            inScope = true;
            break;
          }
        }
      }
      return inScope;
    }

    public String toString() {
      return "SeriesFirstEndDateFilter(" + selectedMonthIds + ", strict:" + showOnlyForActiveMonths + ")";
    }

    protected abstract boolean isEligible(Glob series, GlobRepository repository);
  }

  public static GlobMatcher userCreatedAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedAccount(account);
      }
    };
  }

  public static GlobMatcher userCreatedMainAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedMainAccount(account);
      }
    };
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

  public static GlobMatcher toReconcile() {
    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        return transaction != null && transaction.isTrue(Transaction.TO_RECONCILE);
      }
    };
  }
}
