package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.index.MultiFieldNotUniqueIndex;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.index.UniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class DummyObjectIndex {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static DoubleField VALUE;
  public static IntegerField VALUE_1;
  public static IntegerField VALUE_2;
  public static DateField DATE;

  @NamingField
  public static StringField NAME;

  public static StringField UNIQUE_NAME;

  public static MultiFieldNotUniqueIndex VALUES_INDEX;
  public static MultiFieldUniqueIndex VALUES_AND_NAME_INDEX;
  public static UniqueIndex UNIQUE_NAME_INDEX;
  public static NotUniqueIndex DATE_INDEX;


  static {
    GlobTypeLoader loader = GlobTypeLoader.init(DummyObjectIndex.class);
    loader.defineMultiFieldNotUniqueIndex(VALUES_INDEX, VALUE_1, VALUE_2);
    loader.defineMultiFieldUniqueIndex(VALUES_AND_NAME_INDEX, VALUE_1, VALUE_2, NAME);
    loader.defineUniqueIndex(UNIQUE_NAME_INDEX, UNIQUE_NAME);
    loader.defineNonUniqueIndex(DATE_INDEX, DATE);

  }
}