package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ServerInformation {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static LongField CURRENT_SOFTWARE_VERSION;
  public static LongField LATEST_SOFTWARE_VERSION;
  public static BooleanField MAIL_SEND;

  static {
    GlobTypeLoader.init(ServerInformation.class);
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }
}