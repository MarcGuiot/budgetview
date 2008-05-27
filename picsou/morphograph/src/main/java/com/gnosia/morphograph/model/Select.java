package com.gnosia.morphograph.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Select {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField TITLE;

  public static StringField ALT1;
  public static StringField ALT2;
  public static StringField ALT3;
  public static StringField ALT4;
  public static StringField ALT5;

  public static StringField ANSWERS;

  @Target(Exercise.class)
  public static LinkField EXERCISE;

  static {
    GlobTypeLoader.init(Select.class);
  }
}
