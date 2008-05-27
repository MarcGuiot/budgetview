package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.MultiLineText;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithMultiLineText {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @MultiLineText
  public static StringField COMMENT;

  static {
    GlobTypeLoader.init(DummyObjectWithMultiLineText.class);
  }
}
