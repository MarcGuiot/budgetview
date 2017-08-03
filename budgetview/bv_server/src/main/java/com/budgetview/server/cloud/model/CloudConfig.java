package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CloudConfig {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static BlobField SAMPLE;

  static {
    GlobTypeLoader.init(CloudConfig.class, "cloudConfig");
  }
}
