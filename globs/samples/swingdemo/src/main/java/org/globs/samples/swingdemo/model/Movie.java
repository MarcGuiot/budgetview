package org.globs.samples.swingdemo.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.annotations.Key;

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
