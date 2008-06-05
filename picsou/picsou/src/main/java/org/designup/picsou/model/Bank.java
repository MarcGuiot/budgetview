package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Bank {
  public static final int UNKNOWN_BANK_ID = -123456;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  static {
    GlobTypeLoader.init(Bank.class);
  }
}
