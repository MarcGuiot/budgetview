package org.globs.samples.blog.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.MultiLineText;

public class Category {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  static {
    GlobTypeLoader.init(Category.class);
  }
}
