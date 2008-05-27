package com.gnosia.morphograph.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Exercise {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  @Target(ExerciseType.class)
  public static LinkField EXERCISE_TYPE;

  @Target(Series.class)
  public static LinkField SERIES;

  public static StringField TITLE;
  public static StringField DESCRIPTION;
  public static StringField EXAMPLE;
  public static StringField COMMENT;

  static {
    GlobTypeLoader.init(Exercise.class);
  }
}