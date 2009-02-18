package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ImportedTransaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField DATE;

  public static StringField BANK_DATE;

  public static DoubleField AMOUNT;

  public static StringField NOTE;

  public static StringField OFX_CHECK_NUM;

  public static StringField OFX_NAME;

  public static StringField OFX_MEMO;

  public static StringField QIF_M;

  public static StringField QIF_P;

  public static StringField LABEL;

  public static StringField ORIGINAL_LABEL;

  @Target(Category.class)
  @DefaultInteger(0)
  public static LinkField CATEGORY;

  public static StringField BANK_TRANSACTION_TYPE;

  public static BooleanField IS_CARD;

  public static BooleanField IS_OFX;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  public static BooleanField SPLIT;

  @Target(Transaction.class)
  public static LinkField SPLIT_SOURCE;

  static {
    GlobTypeLoader.init(ImportedTransaction.class);
  }
}