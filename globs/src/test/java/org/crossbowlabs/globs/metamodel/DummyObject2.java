package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObject2 {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  static {
    GlobTypeLoader.init(DummyObject2.class);
  }
}
