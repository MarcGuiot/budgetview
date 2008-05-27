package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithStringKey {
  public static GlobType TYPE;

  @Key public static StringField ID;

  static {
    GlobTypeLoader.init(DummyObjectWithStringKey.class);
  }
}
