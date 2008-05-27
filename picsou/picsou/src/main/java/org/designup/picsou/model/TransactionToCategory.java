package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.index.NotUniqueIndex;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.utils.GlobMatchers;

import java.util.Set;

public class TransactionToCategory {
  public static GlobType TYPE;

  @Key
  @Target(Transaction.class)
  public static LinkField TRANSACTION;

  @Key
  @Target(Category.class)
  public static LinkField CATEGORY;

  public static NotUniqueIndex TRANSACTION_INDEX;

  static {
    GlobTypeLoader.init(TransactionToCategory.class)
            .defineNotUniqueIndex(TRANSACTION_INDEX, TRANSACTION);
  }

  public static void link(GlobRepository repository, Glob transaction, MasterCategory... categories) {
    link(repository, transaction.get(Transaction.ID), categories);
  }

  public static void link(GlobRepository repository, int transactionId, MasterCategory... categories) {
    for (MasterCategory category : categories) {
      if (category != MasterCategory.NONE) {
        link(repository, transactionId, category.getId());
      }
    }
  }

  public static void link(GlobRepository repository, Integer transactionId, Integer[] categoryIds) {
    for (Integer categoryId : categoryIds) {
      link(repository, transactionId, categoryId);
    }
  }

  public static void link(GlobRepository repository, int transactionId, int categoryId) {
    if (categoryId == Category.NONE) {
      GlobList categories = getCategories(transactionId, repository);
      for (Glob category : categories) {
        unlink(repository, transactionId, category.get(Category.ID));
      }
    }
    else {
      repository.findOrCreate(KeyBuilder.createFromValues(TYPE,
                                                          value(TRANSACTION, transactionId),
                                                          value(CATEGORY, categoryId)));
    }
  }

  public static void link(GlobRepository repository, Glob transaction, Glob category) {
    link(repository, transaction.get(Transaction.ID), category.get(Category.ID));
  }

  public static void unlink(GlobRepository repository, Glob transaction, Glob category) {
    unlink(repository, transaction.get(Transaction.ID), category.get(Category.ID));
  }

  private static void unlink(GlobRepository repository, int transactionId, int categoryId) {
    GlobList matchingTransactions = repository.getAll(TYPE, GlobMatchers.fieldEquals(TRANSACTION, transactionId));
    for (Glob matchingTransaction : matchingTransactions) {
      if (matchingTransaction.get(CATEGORY) == categoryId) {
        repository.delete(matchingTransaction.getKey());
      }
    }
  }

  public static boolean hasCategories(Glob transaction, GlobRepository repository) {
    GlobList categories = getCategories(transaction, repository);
    if (categories.isEmpty() ||
        ((categories.size() == 1) && (categories.get(0).get(Category.ID) == Category.NONE))) {
      return false;
    }
    return true;
  }

  public static GlobList getCategories(int transactionId, GlobRepository repository) {
    Set<Integer> categoryIds = repository
            .getAll(TYPE, GlobMatchers.fieldEquals(TRANSACTION, transactionId))
            .getValueSet(CATEGORY);

    return repository.getAll(Category.TYPE, GlobMatchers.contained(Category.ID, categoryIds));
  }

  public static GlobList getCategories(Glob transaction, GlobRepository repository) {
    return getCategories(transaction.get(Transaction.ID), repository);
  }

  public static boolean isInternal(Glob transaction, GlobRepository repository) {
    GlobList categories = getCategories(transaction, repository);
    return ((categories.size() == 1) && (categories.get(0).get(Category.ID) == MasterCategory.INTERNAL.getId()));
  }
}
