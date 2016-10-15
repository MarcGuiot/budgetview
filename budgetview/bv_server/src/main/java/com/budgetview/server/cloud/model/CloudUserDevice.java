package com.budgetview.server.cloud.model;

import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class CloudUserDevice {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static IntegerField USER_ID;

  public static StringField TOKEN;

  public static DateField LAST_UPDATE;

  static {
    GlobTypeLoader.init(CloudUserDevice.class, "cloudUserDevice");
  }
}
