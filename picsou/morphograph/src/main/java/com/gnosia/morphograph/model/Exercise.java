package com.gnosia.morphograph.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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