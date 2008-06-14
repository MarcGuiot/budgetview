package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithCompositeKey {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID1;
  @Key
  public static IntegerField ID2;

  @NamingField
  public static StringField NAME;

  static {
    GlobTypeLoader.init(DummyObjectWithCompositeKey.class);
  }
}
