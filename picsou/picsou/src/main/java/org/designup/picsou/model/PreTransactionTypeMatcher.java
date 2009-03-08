package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class PreTransactionTypeMatcher {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static StringField QIF_M;

  @NoObfuscation
  public static StringField QIF_P;

  @NoObfuscation
  public static StringField OFX_MEMO;

  @NoObfuscation
  public static StringField OFX_NAME;

  @NoObfuscation
  public static StringField OFX_CHECK_NUM;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static StringField ORIGINAL_LABEL;

  @NoObfuscation
  public static StringField BANK_TYPE;

  @NoObfuscation
  public static StringField GROUP_FOR_DATE;

  @NoObfuscation
  public static StringField DATE_FORMAT;

  @Target(TransactionType.class) @NoObfuscation
  public static LinkField TRANSACTION_TYPE;

  @Target(Bank.class)
  @NoObfuscation
  public static LinkField BANK;


  static {
    GlobTypeLoader.init(PreTransactionTypeMatcher.class, "transactionMatcher");
  }
}
