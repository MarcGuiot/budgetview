package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountPosition {
  public static GlobType TYPE;

  @Key
  @Target(AccountEntity.class)
  @NoObfuscation
  public static IntegerField ACCOUNT;

  @Key
  @Target(MonthEntity.class)
  @NoObfuscation
  public static LinkField MONTH;

  @Key
  @NoObfuscation
  public static IntegerField DAY;

  @NoObfuscation
  public static DoubleField POSITION;

  static {
    GlobTypeLoader.init(AccountPosition.class, "accountPosition");
  }
}
