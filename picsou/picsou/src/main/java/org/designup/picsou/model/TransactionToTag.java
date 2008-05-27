package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class TransactionToTag {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Transaction.class)
  public static LinkField TRANSACTION;

  @Target(Tag.class)
  public static LinkField TAG;

  static {
    GlobTypeLoader.init(TransactionToTag.class);
  }
}
