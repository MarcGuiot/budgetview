package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.DateField;
import org.crossbowlabs.globs.metamodel.fields.DoubleField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class TransactionImport {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  public static StringField SOURCE;
  public static DateField IMPORT_DATE;
  public static DateField LAST_TRANSACTION_DATE;
  public static DoubleField BALANCE;

  static {
    GlobTypeLoader.init(TransactionImport.class);
  }
}
