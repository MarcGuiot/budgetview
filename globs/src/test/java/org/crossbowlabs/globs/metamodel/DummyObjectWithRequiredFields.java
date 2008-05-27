package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Required;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithRequiredFields {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static IntegerField VALUE;

  @Required
  public static StringField NAME;

  static {
    GlobTypeLoader.init(DummyObjectWithRequiredFields.class);
  }
}
