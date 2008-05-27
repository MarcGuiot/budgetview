package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Tag {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  @Target(TagCategory.class)
  public static LinkField CATEGORY_ID;

  public static StringField VALUE;

  static {
    GlobTypeLoader.init(Tag.class);
  }
}
