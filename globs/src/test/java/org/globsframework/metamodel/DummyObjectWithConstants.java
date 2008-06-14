package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;

public enum DummyObjectWithConstants implements GlobConstantContainer {
  CONSTANT_1(1),
  CONSTANT_2(2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private ReadOnlyGlob glob;
  private int id;

  DummyObjectWithConstants(int id) {
    this.id = id;
  }

  public ReadOnlyGlob getGlob() {
    if (glob == null) {
      this.glob = new ReadOnlyGlob(TYPE,
                                   FieldValuesBuilder
                                     .init(ID, id)
                                     .get());
    }
    return glob;
  }

  static {
    GlobTypeLoader.init(DummyObjectWithConstants.class);
  }
}
