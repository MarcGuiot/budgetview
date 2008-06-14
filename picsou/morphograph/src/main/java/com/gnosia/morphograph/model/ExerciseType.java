package com.gnosia.morphograph.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;

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
