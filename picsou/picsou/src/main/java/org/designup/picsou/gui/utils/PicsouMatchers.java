package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PicsouMatchers {
  private PicsouMatchers() {
  }

  public static GlobMatcher transactionsForAccounts(Set<Integer> accountIds) {
    if (accountIds.contains(Account.SUMMARY_ACCOUNT_ID)) {
      return GlobMatchers.ALL;
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
}
