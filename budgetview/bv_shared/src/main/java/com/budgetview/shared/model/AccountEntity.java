package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountEntity {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static BooleanField IS_USER_ACCOUNT;

  @Target(MonthEntity.class)
  @NoObfuscation
  public static LinkField POSITION_MONTH;

  @NoObfuscation
  public static IntegerField POSITION_DAY;

  @NoObfuscation
  public static DoubleField POSITION;

  @NoObfuscation
  public static IntegerField ACCOUNT_TYPE;

  @NoObfuscation
  public static IntegerField SEQUENCE_NUMBER;

  public static Integer ACCOUNT_ID_MAIN = -1;
  public static Integer ACCOUNT_ID_SAVINGS = -2;

  public static Integer ACCOUNT_TYPE_MAIN = -1;
  public static Integer ACCOUNT_TYPE_SAVINGS = -2;

  static {
    GlobTypeLoader.init(AccountEntity.class, "accountEntity");
  }
}
