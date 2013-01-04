package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AppVersionInformation {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LATEST_AVALAIBLE_SOFTWARE_VERSION;
  public static LongField LATEST_AVALAIBLE_JAR_VERSION;
  public static LongField LATEST_BANK_CONFIG_SOFTWARE_VERSION;

  static {
    GlobTypeLoader.init(AppVersionInformation.class, "appVersionInformation");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }
}