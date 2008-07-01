package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ProfileType {
  public static GlobType TYPE;

  @Key public static IntegerField ID;

  public static StringField NAME;

  static {
    GlobTypeLoader.init(ProfileType.class);
  }
}