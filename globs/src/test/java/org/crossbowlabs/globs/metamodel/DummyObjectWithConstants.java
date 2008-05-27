package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.impl.ReadOnlyGlob;
import org.crossbowlabs.globs.model.utils.GlobConstantContainer;

public enum DummyObjectWithConstants implements GlobConstantContainer {
  CONSTANT_1(1),
  CONSTANT_2(2);

  public static GlobType TYPE;

  @Key public static IntegerField ID;

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
