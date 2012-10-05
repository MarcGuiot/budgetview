package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ImportToAccount {
  public static GlobType TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT_ID;

  @Key
  @Target(TransactionImport.class)
  public static LinkField IMPORT_ID;

  static {
    GlobTypeLoader.init(ImportToAccount.class);
  }

}
