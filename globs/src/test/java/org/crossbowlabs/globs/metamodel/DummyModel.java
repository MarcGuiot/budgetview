package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.utils.DefaultGlobModel;

public class DummyModel {
  private static GlobModel globModel;

  public static GlobModel get() {
    return globModel;
  }

  static {
    globModel = new DefaultGlobModel(
      DummyObject.TYPE,
      DummyObject2.TYPE,
      DummyObjectWithLinks.TYPE,
      DummyObjectWithCompositeKey.TYPE,
      DummyObjectWithStringKey.TYPE,
      DummyObjectWithLinkFieldId.TYPE,
      DummyObjectIndex.TYPE
    );
  }
}
