package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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
