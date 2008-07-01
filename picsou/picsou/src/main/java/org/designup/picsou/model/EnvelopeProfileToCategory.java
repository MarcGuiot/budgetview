package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;

public class EnvelopeProfileToCategory {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(EnvelopeProfile.class)
  public static LinkField ENVELOPE;

  @Target(Category.class)
  public static LinkField CATEGORY;

  static {
    GlobTypeLoader.init(EnvelopeProfileToCategory.class);
  }
}
