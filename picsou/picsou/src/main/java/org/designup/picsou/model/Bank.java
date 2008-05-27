package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Bank {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(Bank.class);
  }
}
