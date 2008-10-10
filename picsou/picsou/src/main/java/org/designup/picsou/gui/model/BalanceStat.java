package org.designup.picsou.gui.model;

import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BalanceStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static IntegerField MONTH_ID;

  @DefaultDouble(0.0)
  public static DoubleField BEGIN_OF_MONTH_ACCOUNT_BALANCE;

  @DefaultDouble(0.0)
  public static DoubleField END_OF_MONTH_ACCOUNT_BALANCE;

  @DefaultDouble(0.0)
  public static DoubleField MONTH_BALANCE;

  public static DoubleField LAST_KNOWN_ACCOUNT_BALANCE;

  public static IntegerField LAST_KNOWN_ACCOUNT_BALANCE_DAY;

  @DefaultDouble(0.0)
  public static DoubleField INCOME;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField ENVELOPES;

  @DefaultDouble(0.0)
  public static DoubleField ENVELOPES_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField OCCASIONAL;

  @DefaultDouble(0.0)
  public static DoubleField OCCASIONAL_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SPECIAL;

  @DefaultDouble(0.0)
  public static DoubleField SPECIAL_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField UNCATEGORIZED;

  static {
    GlobTypeLoader.init(BalanceStat.class);
  }
}