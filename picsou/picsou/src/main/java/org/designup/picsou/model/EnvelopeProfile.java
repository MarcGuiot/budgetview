package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class EnvelopeProfile {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(EnvelopeProfile.class);
  }
}
