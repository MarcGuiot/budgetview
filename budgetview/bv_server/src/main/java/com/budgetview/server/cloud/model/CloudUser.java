package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CloudUser {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static StringField EMAIL;

  @NoObfuscation
  public static StringField BUDGEA_ACCESS_TOKEN;

  static {
    GlobTypeLoader.init(CloudUser.class, "cloudUser");
  }
}
