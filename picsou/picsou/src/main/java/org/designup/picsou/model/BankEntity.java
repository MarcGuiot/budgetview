package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.index.NotUniqueIndex;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class BankEntity {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Bank.class)
  public static LinkField BANK;

  public static NotUniqueIndex BANK_INDEX;

  static {
    GlobTypeLoader.init(BankEntity.class)
      .defineNotUniqueIndex(BANK_INDEX, BANK);
  }

}
