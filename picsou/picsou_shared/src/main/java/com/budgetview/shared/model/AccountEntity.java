package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountEntity {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  @Target(MonthEntity.class)
  public static LinkField POSITION_MONTH;

  public static IntegerField POSITION_DAY;

  public static DoubleField POSITION;

  public static IntegerField ACCOUNT_TYPE;

  public static IntegerField SEQUENCE_NUMBER;

  public static Integer ACCOUNT_TYPE_MAIN = 1;
  public static Integer ACCOUNT_TYPE_SAVINGS = 2;

  static {
    GlobTypeLoader.init(AccountEntity.class, "accountEntity");
  }
}
