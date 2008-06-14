package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithStringKey {
  public static GlobType TYPE;

  @Key
  public static StringField ID;

  static {
    GlobTypeLoader.init(DummyObjectWithStringKey.class);
  }
}
