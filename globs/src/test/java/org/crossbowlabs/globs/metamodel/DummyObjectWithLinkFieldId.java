package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithLinkFieldId {
  public static GlobType TYPE;

  @Key @Target(DummyObject.class)
  public static LinkField LINK;

  static {
    GlobTypeLoader.init(DummyObjectWithLinkFieldId.class);
  }
}
