package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.DefaultInteger;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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