package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
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

  public static IntegerField ACCOUNT_COUNT;

  public static DoubleField SUMMARY_POSITION_AT_MIN;

  static {
    GlobTypeLoader.init(AccountStat.class);
  }
}
