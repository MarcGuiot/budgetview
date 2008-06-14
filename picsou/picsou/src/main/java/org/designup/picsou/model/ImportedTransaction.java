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
  public static StringField BANK_MEMO;
  public static StringField LABEL;
  public static StringField NOTE;
  public static StringField ORIGINAL_LABEL;
  public static BooleanField DISPENSABLE; // unused
  public static StringField LABEL_FOR_CATEGORISATION;

  @Target(Category.class)
  @DefaultInteger(0)
  public static LinkField CATEGORY;

  public static StringField BANK_TRANSACTION_TYPE;
  public static BooleanField IS_CARD;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  public static BooleanField SPLIT;

  @Target(Transaction.class)
  public static LinkField SPLIT_SOURCE;

  static {
    GlobTypeLoader.init(ImportedTransaction.class);
  }
}