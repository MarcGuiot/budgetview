package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.MultiLineText;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
