package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class TransactionTypeMatcher {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField REGEXP;
  public static IntegerField GROUP_FOR_LABEL;
  public static IntegerField GROUP_FOR_DATE;

  @Target(TransactionType.class)
  public static LinkField TRANSACTION_TYPE;

  @Target(Bank.class)
  public static LinkField BANK;

  static {
    GlobTypeLoader.init(TransactionTypeMatcher.class);
  }
}
