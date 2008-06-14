package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
