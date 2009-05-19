package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;

import java.util.*;

public class PicsouMatchers {
  private PicsouMatchers() {
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
    return GlobMatchers.contained(Transaction.MONTH, months);
  }

  public static GlobMatcher transactionsForCategories(final Collection<Integer> categoryIds, final GlobRepository repository) {
    if (categoryIds.isEmpty()) {
      return GlobMatchers.NONE;
    }
    if (categoryIds.contains(Category.ALL)) {
      return GlobMatchers.ALL;
    }

    final boolean includeNonAffectedTransactions = categoryIds.contains(Category.NONE);

    final Set<Integer> extendedIdSet = extendToMasterIds(categoryIds, repository);

    return new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        Integer categoryId = transaction.get(Transaction.CATEGORY);
        if (categoryId != null) {
          return extendedIdSet.contains(categoryId);
        }
        return includeNonAffectedTransactions;
      }

      public String toString() {
        return "transaction.category in " + categoryIds;
      }
    };
  }

  private static Set<Integer> extendToMasterIds(Collection<Integer> categoryIds, GlobRepository repository) {
    final Set<Integer> extendedIdSet = new HashSet<Integer>();
    extendedIdSet.addAll(categoryIds);
    for (Integer categoryId : categoryIds) {
      if (categoryId == null) {
        continue;
      }
      Glob category = repository.get(Key.create(Category.TYPE, categoryId));
      if (Category.isMaster(category)) {
        Set<Integer> subcategoryIds =
          repository.getAll(Category.TYPE, fieldEquals(Category.MASTER, categoryId)).getValueSet(Category.ID);
        extendedIdSet.addAll(subcategoryIds);
      }
    }
    return extendedIdSet;
  }

  public static GlobMatcher masterCategories() {
    return GlobMatchers.isNull(Category.MASTER);
  }

  public static GlobMatcher masterUserCategories() {
    return and(GlobMatchers.isNull(Category.MASTER),
               not(
                 GlobMatchers.fieldContained(Category.ID, MasterCategory.RESERVED_CATEGORY_IDS)));
  }

  public static GlobMatcher subCategories(Integer masterCategoryId) {
    return fieldEquals(Category.MASTER, masterCategoryId);
  }

  public static GlobMatcher transactionsForSeries(final Set<Integer> targetBudgetAreas,
                                                  Set<Integer> targetSeries,
                                                  GlobRepository repository) {
    if (targetBudgetAreas.contains(BudgetArea.ALL.getId())) {
      return GlobMatchers.ALL;
    }
    final Set<Integer> reducedSeriesSet = new HashSet<Integer>();
    for (Integer seriesId : targetSeries) {
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      if (!targetBudgetAreas.contains(series.get(Series.BUDGET_AREA))) {
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
      not(fieldEquals(Transaction.PLANNED, Boolean.TRUE)),
      not(fieldEquals(Transaction.MIRROR, Boolean.TRUE))
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
              return series.get(Series.IS_MIRROR);
            }
            if (toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
              return !series.get(Series.IS_MIRROR);
            }
            return false;
          }
          if (accountId == Account.SAVINGS_SUMMARY_ACCOUNT_ID) {
            if (fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
              return series.get(Series.IS_MIRROR);
            }
            if (toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
              return !series.get(Series.IS_MIRROR);
            }
            return false;
          }
          if (accountId.equals(fromAccount.get(Account.ID))) {
            return series.get(Series.IS_MIRROR);
          }
          if (accountId.equals(toAccount.get(Account.ID))) {
            return !series.get(Series.IS_MIRROR);
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

  public static CategorizationFilter seriesFilter(final Integer budgetAreaId) {
    return new CategorizationFilter(budgetAreaId);
  }

  public static class CategorizationFilter implements GlobMatcher {
    private List<Glob> transactions;
    private SeriesFirstEndDateFilter filter;

    public CategorizationFilter(final Integer budgetAreaId) {
      filter = seriesDateFilter(budgetAreaId, true);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (filter.matches(series, repository)) {
        Integer toAccountId = series.get(Series.TO_ACCOUNT);
        Integer fromAccountId = series.get(Series.FROM_ACCOUNT);
        Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
        Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
        if (Account.onlyOneIsImported(toAccount, fromAccount)) {
          for (Glob transaction : transactions) {
            if (transaction.get(Transaction.AMOUNT) > 0) {
              if (!toAccount.get(Account.IS_IMPORTED_ACCOUNT)) {
                return false;
              }
            }
            else {
              if (!fromAccount.get(Account.IS_IMPORTED_ACCOUNT)) {
                return false;
              }
            }
          }
        }
        else if (toAccountId != null && fromAccountId != null) {
          for (Glob transaction : transactions) {
            if (transaction.get(Transaction.AMOUNT) > 0) {
              if (series.get(Series.IS_MIRROR)) {
                return false;
              }
              if (!toAccountId.equals(transaction.get(Transaction.ACCOUNT))) {
                return false;
              }
            }
            else {
              if (!series.get(Series.IS_MIRROR)) {
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
        if (firstMonth == null && lastMonth == null) {
          for (Integer id : monthIds) {
            BooleanField monthField = Series.getMonthField(id);
            if (series.get(monthField) != exclusive) {
              return !exclusive;
            }
          }
          return true;
        }
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
          BooleanField monthField = Series.getMonthField(id);
          if (!series.get(monthField)){
            Glob seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
              .findByIndex(SeriesBudget.MONTH, id).getGlobs().getFirst();
            if (seriesBudget != null && !seriesBudget.get(SeriesBudget.ACTIVE)){
              return false;
            }
          }
        }
        return exclusive;  // return true?
      }
      return false;
    }

    protected abstract boolean isEligible(Glob series, GlobRepository repository);
  }
}
