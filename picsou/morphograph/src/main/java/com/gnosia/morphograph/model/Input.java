package com.gnosia.morphograph.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Input {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField TITLE;
  public static StringField ANSWER;

  @Target(Exercise.class)
  public static LinkField EXERCISE;

  static {
    GlobTypeLoader.init(Input.class);
  }
}