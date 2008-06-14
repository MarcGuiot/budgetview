package org.globs.samples.blog.model;

import org.globsframework.globs.metamodel.GlobType;
import org.globsframework.globs.metamodel.utils.GlobTypeLoader;
import org.globsframework.globs.metamodel.fields.IntegerField;
import org.globsframework.globs.metamodel.fields.StringField;
import org.globsframework.globs.metamodel.annotations.Key;
import org.globsframework.globs.metamodel.annotations.NamingField;
import org.globsframework.globs.metamodel.annotations.MultiLineText;

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
