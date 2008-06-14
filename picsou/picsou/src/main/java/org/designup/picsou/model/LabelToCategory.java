package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
