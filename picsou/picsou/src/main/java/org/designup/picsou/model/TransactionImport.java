package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
