package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.DefaultInteger;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.metamodel.index.NotUniqueIndex;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import static org.crossbowlabs.globs.model.Key.create;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.Utils;

public class Transaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField MONTH; // yyyymm format
  public static IntegerField DAY; // Starts at 1
  public static DoubleField AMOUNT;
  public static StringField LABEL;
  public static StringField NOTE;
  public static StringField ORIGINAL_LABEL;
  public static BooleanField DISPENSABLE; // unused
  public static StringField LABEL_FOR_CATEGORISATION;

  @Target(Category.class)
  @DefaultInteger(0)
  public static LinkField CATEGORY;

  @Target(TransactionType.class)
  public static LinkField TRANSACTION_TYPE;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(TransactionImport.class)
  public static LinkField IMPORT;

  public static BooleanField SPLIT;

  @Target(Transaction.class)
  public static LinkField SPLIT_SOURCE;

  public static NotUniqueIndex LABEL_FOR_CATEGORISATION_INDEX;

  static {
    GlobTypeLoader.init(Transaction.class)
      .defineNotUniqueIndex(LABEL_FOR_CATEGORISATION_INDEX, LABEL_FOR_CATEGORISATION);
  }

  public static int fullDate(Glob transaction) {
    return transaction.get(MONTH) * 100 + transaction.get(DAY);
  }

  public static String stringifyCategories(Glob transaction,
                                           GlobRepository repository,
                                           GlobStringifier categoryStringifier) {
    GlobList categories = getCategories(transaction, repository).sort(Category.ID);
    int index = 0;
    StringBuilder builder = new StringBuilder();
    for (Glob category : categories) {
      if (index++ > 0) {
        builder.append(", ");
      }
      builder.append(categoryStringifier.toString(category, repository));
    }
    return builder.toString();
  }

  public static boolean isCategorized(Glob transaction, GlobRepository repository) {
    return !hasNoCategory(transaction) || TransactionToCategory.hasCategories(transaction, repository);
  }

  public static boolean hasNoCategory(Glob transaction) {
    Integer categoryId = transaction.get(CATEGORY);
    return (categoryId == null) || Utils.equal(categoryId, Category.NONE);
  }

  public static GlobList getCategories(Glob transaction, GlobRepository repository) {
    GlobList categories = TransactionToCategory.getCategories(transaction.get(Transaction.ID), repository);
    if (!hasNoCategory(transaction)) {
      categories.add(0, repository.get(create(Category.TYPE, transaction.get(CATEGORY))));
    }
    return categories;
  }

  public static void setCategory(Glob transaction, Integer categoryId, GlobRepository repository) {
    repository.enterBulkDispatchingMode();

    try {
      repository.setTarget(transaction.getKey(), CATEGORY, create(Category.TYPE, categoryId));
      repository.delete(repository.findByIndex(TransactionToCategory.TRANSACTION_INDEX,
                                               transaction.get(Transaction.ID)));
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public static void setCategory(Glob transaction, Glob category, GlobRepository repository) {
    setCategory(transaction, category.get(Category.ID), repository);
  }

  public static boolean isSplitPart(Glob transaction) {
    return transaction.get(SPLIT_SOURCE) != null;
  }

  public static boolean isSplitSource(Glob transaction) {
    return Boolean.TRUE.equals(transaction.get(SPLIT));
  }

  public static boolean isSplit(Glob transaction) {
    return isSplitSource(transaction) || isSplitPart(transaction);
  }

  public static double subtract(double initialValue, double amount) {
    return Math.rint(Math.signum(initialValue) * (Math.abs(initialValue) - Math.abs(amount)) * 100.0) / 100.0;
  }
}
