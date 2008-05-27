package org.globs.samples.blog.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.metamodel.annotations.*;

public class Post {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField TITLE;

  @MultiLineText
  public static StringField CONTENT;

  @DefaultDate()
  public static DateField PUBLICATION_DATE;

  @DefaultBoolean(false)
  public static BooleanField PUBLISHED;

  @Target(Category.class)
  public static LinkField CATEGORY;

  static {
    GlobTypeLoader.init(Post.class);
  }
}
