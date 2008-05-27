package org.globs.samples.swingdemo.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.fields.DateField;
import org.crossbowlabs.globs.metamodel.annotations.Key;

public class Movie {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField TITLE;
  public static DateField DATE;
  public static StringField DIRECTOR;

  static {
    GlobTypeLoader.init(Movie.class);
  }
}
