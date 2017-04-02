package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class CloudEmailValidation {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static StringField CODE;

  public static StringField EMAIL;

  @Target(CloudUser.class)
  public static LinkField USER;

  public static TimeStampField CREATION_DATE;

  public static TimeStampField EXPIRATION_DATE;

  @DefaultBoolean(false)
  public static BooleanField VALIDATED;

  static {
    GlobTypeLoader.init(CloudEmailValidation.class, "cloudEmailValidation");
  }
}
