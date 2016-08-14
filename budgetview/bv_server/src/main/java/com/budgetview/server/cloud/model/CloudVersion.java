package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.KeyBuilder;

public class CloudVersion {
  public static GlobType TYPE;

  public static org.globsframework.model.Key key;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static IntegerField MAJOR_VERSION;

  @NoObfuscation
  public static IntegerField MINOR_VERSION;

  static {
    GlobTypeLoader.init(CloudVersion.class, "cloudVersion");
    key = KeyBuilder.newKey(TYPE, 0);
  }


}
