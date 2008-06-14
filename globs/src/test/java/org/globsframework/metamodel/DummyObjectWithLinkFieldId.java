package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithLinkFieldId {
  public static GlobType TYPE;

  @Key
  @Target(DummyObject.class)
  public static LinkField LINK;

  static {
    GlobTypeLoader.init(DummyObjectWithLinkFieldId.class);
  }
}
