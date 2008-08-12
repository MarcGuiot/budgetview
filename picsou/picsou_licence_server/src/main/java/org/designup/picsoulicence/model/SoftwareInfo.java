package org.designup.picsoulicence.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class SoftwareInfo {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static LongField LATEST_JAR_VERSION;

  public static LongField LATEST_CONFIG_VERSION;

  static {
    GlobTypeLoader.init(SoftwareInfo.class);
  }

}