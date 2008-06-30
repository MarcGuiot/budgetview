package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.KeyBuilder;

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
  public static DoubleField DISPENSABLE;


  static {
    GlobTypeLoader.init(MonthStat.class);
  }

  public static org.globsframework.model.Key getKey(Integer month, Integer categoryId, int accountId) {
    return KeyBuilder.init(MONTH, month)
      .setValue(CATEGORY, categoryId)
      .setValue(ACCOUNT, accountId)
      .get();
  }
}
