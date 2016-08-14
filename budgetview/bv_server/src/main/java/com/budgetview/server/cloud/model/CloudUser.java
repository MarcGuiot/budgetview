package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class CloudUser {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static StringField EMAIL;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ID;

  public static StringField PROVIDER_ACCESS_TOKEN;

  static {
    GlobTypeLoader.init(CloudUser.class, "cloudUser");
  }
}
