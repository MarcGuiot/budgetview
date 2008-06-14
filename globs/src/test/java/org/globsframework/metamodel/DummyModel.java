package org.globsframework.metamodel;

import org.globsframework.metamodel.utils.DefaultGlobModel;

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
