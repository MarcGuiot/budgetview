package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.annotations.MaxSize;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithMaxSizeString {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField @MaxSize(10)
  public static StringField TEXT;

  static {
    GlobTypeLoader.init(DummyObjectWithMaxSizeString.class);
  }
}
