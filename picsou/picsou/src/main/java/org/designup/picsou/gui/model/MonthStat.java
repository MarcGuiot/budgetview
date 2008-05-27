package org.designup.picsou.gui.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.index.MultiFieldUniqueIndex;
import org.crossbowlabs.globs.metamodel.annotations.DefaultDouble;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.DoubleField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.KeyBuilder;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;

public class MonthStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static IntegerField MONTH;
  @Key
  @Target(Category.class)
  public static LinkField CATEGORY;
  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSES;
  @DefaultDouble(0.0)
  public static DoubleField INCOME;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSES_PART;
  @DefaultDouble(0.0)
  public static DoubleField INCOME_PART;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSES_AVERAGE;
  @DefaultDouble(0.0)
  public static DoubleField INCOME_AVERAGE;

  @DefaultDouble(0.0)
  public static DoubleField DISPENSABLE;
  @DefaultDouble(0.0)
  public static DoubleField DISPENSABLE_AVERAGE;


//  public static MultiFieldUniqueIndex ACCOUNT_MONTH_CATEGORY;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(MonthStat.class);
//    loader.defineMultiFieldUniqueIndex(ACCOUNT_MONTH_CATEGORY, ACCOUNT, MONTH, CATEGORY);
  }

  public static org.crossbowlabs.globs.model.Key getKey(Integer month, Integer categoryId, int accountId) {
    return KeyBuilder.init(MONTH, month)
      .setValue(CATEGORY, categoryId)
      .setValue(ACCOUNT, accountId)
      .get();
  }
}
