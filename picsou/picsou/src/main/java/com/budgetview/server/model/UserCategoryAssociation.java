package com.budgetview.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.MaxSize;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class UserCategoryAssociation {

  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static LongField USER_ID;

  @MaxSize(100)
  public static StringField INFO;

  public static IntegerField CATEGORY_ID;
  public static IntegerField COUNT;

  static {
    GlobTypeLoader.init(UserCategoryAssociation.class);
  }
}
