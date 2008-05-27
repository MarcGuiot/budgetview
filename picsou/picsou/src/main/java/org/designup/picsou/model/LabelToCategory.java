package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.index.NotUniqueIndex;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class LabelToCategory {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  @Target(Category.class)
  public static LinkField CATEGORY;

  public static IntegerField COUNT;

  public static NotUniqueIndex LABEL_INDEX;

  static {
    GlobTypeLoader.init(LabelToCategory.class)
      .defineNotUniqueIndex(LABEL_INDEX, LABEL);
  }
}
