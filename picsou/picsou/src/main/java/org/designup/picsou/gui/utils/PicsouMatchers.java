package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SameAccountChecker;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
          GlobMatchers.not(GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId())), repository)
          .getValueSet(Account.ID));
    }
    if (accountIds.contains(Account.SAVINGS_SUMMARY_ACCOUNT_ID)) {
      accountIds.addAll(
        repository.getAll(Account.TYPE)
          .filterSelf(GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()), repository)
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
          repository.getAll(Category.TYPE, GlobMatchers.fieldEquals(Category.MASTER, categoryId)).getValueSet(Category.ID);
        extendedIdSet.addAll(subcategoryIds);
      }
    }
    return extendedIdSet;
  }

  public static GlobMatcher masterCategories() {
    return GlobMatchers.isNull(Category.MASTER);
  }

  public static GlobMatcher masterUserCategories() {
    return GlobMatchers.and(GlobMatchers.isNull(Category.MASTER),
                            GlobMatchers.not(
                              GlobMatchers.fieldContained(Category.ID, MasterCategory.RESERVED_CATEGORY_IDS)));
  }

  public static GlobMatcher subCategories(Integer masterCategoryId) {
    return GlobMatchers.fieldEquals(Category.MASTER, masterCategoryId);
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

  public static SeriesFirstEndDateFilter seriesDateFilter(Integer budgetAreaId, boolean isExclusive) {
    return new SeriesFirstEndDateFilter(budgetAreaId, isExclusive);
  }

  static public class SeriesFirstEndDateFilter implements GlobMatcher {
    private Integer budgetAreaId;
    private boolean exclusive;
    private Set<Integer> monthIds = Collections.emptySet();
    private Set<Integer> accounts = Collections.emptySet();

    private SeriesFirstEndDateFilter(Integer budgetAreaId, boolean isExclusive) {
      this.budgetAreaId = budgetAreaId;
      exclusive = isExclusive;
    }

    public void filterDates(Set<Integer> monthIds, Set<Integer> accounts) {
      this.monthIds = monthIds;
      this.accounts = accounts;
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (budgetAreaId.equals(series.get(Series.BUDGET_AREA))) {
        Integer toAccount = series.get(Series.TO_ACCOUNT);
        Integer fromAccount = series.get(Series.FROM_ACCOUNT);
        if (exclusive) {
          if (toAccount == null && fromAccount == null) {
            SameAccountChecker mainAccountChecker = SameAccountChecker.getSameAsMain(repository);
            for (Integer account : accounts) {
              if (!mainAccountChecker.isSame(account)) {
                return false;
              }
            }
          }
          else {
            if (series.get(Series.IS_MIROR)) {
              return false;
            }
            SameAccountChecker mainAccountChecker = SameAccountChecker.getSameAsMain(repository);
            for (Integer account : accounts) {
              if (toAccount != null && (account.equals(toAccount) || (toAccount == Account.MAIN_SUMMARY_ACCOUNT_ID && mainAccountChecker.isSame(account)))) {
                continue;
              }
              if (fromAccount != null && (account.equals(fromAccount) || (fromAccount == Account.MAIN_SUMMARY_ACCOUNT_ID && mainAccountChecker.isSame(account)))) {
                continue;
              }
              return false;
            }
          }
        }

        Integer firstMonth = series.get(Series.FIRST_MONTH);
        Integer lastMonth = series.get(Series.LAST_MONTH);
        if (firstMonth == null && lastMonth == null) {
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
        }
        return exclusive;
      }
      return false;
    }
  }
}
