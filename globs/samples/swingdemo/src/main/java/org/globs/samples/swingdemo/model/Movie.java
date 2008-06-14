package org.globs.samples.swingdemo.model;

import org.globsframework.globs.metamodel.GlobType;
import org.globsframework.globs.metamodel.utils.GlobTypeLoader;
import org.globsframework.globs.metamodel.fields.IntegerField;
import org.globsframework.globs.metamodel.fields.StringField;
import org.globsframework.globs.metamodel.fields.DateField;
import org.globsframework.globs.metamodel.annotations.Key;

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
