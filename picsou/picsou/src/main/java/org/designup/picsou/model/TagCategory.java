package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class TagCategory {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  @Target(TagType.class)
  public static LinkField TAG_TYPE_ID;

  static {
    GlobTypeLoader.init(TagCategory.class);
  }

  //

  // Gratabilité   ==> TagCategory
  //   |_ 0    ==> Tag
  //   |_ 1
  //   |_ 2
  // Tag.visit()
  // 
}
