package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
