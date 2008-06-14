package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
