package org.designup.picsou.license.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class SoftwareInfo {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static LongField LATEST_JAR_VERSION;

  public static LongField LATEST_CONFIG_VERSION;

  public static StringField MAIL;

  public static IntegerField GROUP_ID;

  static {
    GlobTypeLoader.init(SoftwareInfo.class);
  }

}