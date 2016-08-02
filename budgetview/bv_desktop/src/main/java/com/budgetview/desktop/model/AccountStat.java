package com.budgetview.desktop.model;

import com.budgetview.model.Account;
import com.budgetview.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(Account.class)
  public static LinkField MIN_ACCOUNT;

  public static DoubleField BEGIN_POSITION;

  public static DoubleField MIN_POSITION;

  public static DoubleField FUTURE_MIN_POSITION;

  public static DoubleField END_POSITION;

  @DefaultInteger(0)
  public static IntegerField ACCOUNT_COUNT;

  public static DoubleField SUMMARY_POSITION_AT_MIN;

  static {
    GlobTypeLoader.init(AccountStat.class);
  }
}
