package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SeriesToCategory {
  public static GlobType TYPE;

  @NoObfuscation
  @Key
  public static IntegerField ID;

  @NoObfuscation
  @Target(Series.class)
  public static LinkField SERIES;

  @NoObfuscation
  @Target(Category.class)
  public static LinkField CATEGORY;

  static {
    GlobTypeLoader.init(SeriesToCategory.class, "seriesToCategory");
  }
}
