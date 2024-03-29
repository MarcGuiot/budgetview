package com.budgetview.desktop.model;

import com.budgetview.model.Account;
import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;

public class PeriodAccountStat {
  public static GlobType TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultBoolean(true)
  public static BooleanField OK;

  @DefaultInteger(0)
  public static IntegerField UNCATEGORIZED_COUNT;

  public static IntegerField SEQUENCE;

  static {
    TypeLoader.init(PeriodAccountStat.class, "periodAccountStat");
  }
}
