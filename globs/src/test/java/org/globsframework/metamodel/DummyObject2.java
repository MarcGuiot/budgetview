package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class DummyObject2 {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  static {
    GlobTypeLoader.init(DummyObject2.class);
  }
}
