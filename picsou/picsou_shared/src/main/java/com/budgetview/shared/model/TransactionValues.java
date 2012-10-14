package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class TransactionValues {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(SeriesValues.class)
  public static LinkField SERIES_VALUES;

  public static StringField LABEL;

  public static DoubleField AMOUNT;

  public static IntegerField BANK_MONTH;
  public static IntegerField BANK_DAY;

  public static IntegerField SEQUENCE_NUMBER;

  static {
    GlobTypeLoader.init(TransactionValues.class, "transactionValues");
  }
}
