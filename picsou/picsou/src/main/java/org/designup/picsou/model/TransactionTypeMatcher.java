package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class TransactionTypeMatcher {

  public static GlobType TYPE;

  @NoObfuscation
  @Key
  public static IntegerField ID;

  @NoObfuscation
  public static StringField REGEXP;
  @NoObfuscation
  public static StringField LABEL;
  @NoObfuscation
  public static IntegerField GROUP_FOR_DATE;
  @NoObfuscation
  public static StringField DATE_FORMAT;

  @NoObfuscation
  @Target(TransactionType.class)
  public static LinkField TRANSACTION_TYPE;

  @NoObfuscation
  @Target(Bank.class)
  public static LinkField BANK;

  static {
    GlobTypeLoader.init(TransactionTypeMatcher.class, "transactionTypeMatcher");
  }
}
