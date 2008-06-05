package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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
