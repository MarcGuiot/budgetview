package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Required;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithRequiredLink {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField TARGET_ID;

  public static StringField NAME;

  @Required
  public static Link LINK;

  static {
    GlobTypeLoader.init(DummyObjectWithRequiredLink.class)
          .defineLink(LINK).add(TARGET_ID, DummyObject.ID);
  }
}
