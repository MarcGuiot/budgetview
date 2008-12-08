package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SavingsBalanceStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultDouble(0.0)
  public static DoubleField BEGIN_OF_MONTH_POSITION;

  @DefaultDouble(0.0)
  public static DoubleField END_OF_MONTH_POSITION;

  @DefaultDouble(0.0)
  public static DoubleField BALANCE;

  public static DoubleField LAST_KNOWN_ACCOUNT_POSITION;

  public static IntegerField LAST_KNOWN_POSITION_DAY;

  @DefaultDouble(0.0)
  public static DoubleField OUT;

  @DefaultDouble(0.0)
  public static DoubleField OUT_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField OUT_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_PLANNED;

  static {
    GlobTypeLoader.init(SavingsBalanceStat.class);
  }
}