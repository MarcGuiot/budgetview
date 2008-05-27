package com.gnosia.morphograph.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.impl.ReadOnlyGlob;
import org.crossbowlabs.globs.model.utils.GlobConstantContainer;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public enum ExerciseType implements GlobConstantContainer {
  INPUT(1),
  SELECT(2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private ReadOnlyGlob glob;
  private int id;

  ExerciseType(int id) {
    this.id = id;
  }

  public static ExerciseType get(Glob exercise) {
    Integer id = exercise.get(Exercise.EXERCISE_TYPE);
    for (ExerciseType type : values()) {
      if (id == type.id) {
        return type;
      }
    }
    throw new ItemNotFound("No exercise type '" + id + "'found for exercise " + exercise);
  }

  public ReadOnlyGlob getGlob() {
    if (glob == null) {
      glob = new ReadOnlyGlob(TYPE,
                              FieldValuesBuilder
                                .init(ID, id)
                                .set(NAME, Strings.toNiceLowerCase(name()))
                                .get());
    }
    return glob;
  }

  static {
    GlobTypeLoader.init(ExerciseType.class);
  }
}
