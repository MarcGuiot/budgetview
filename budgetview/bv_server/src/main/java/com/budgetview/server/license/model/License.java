package com.budgetview.server.license.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultLong;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class License {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static StringField MAIL;

  public static StringField ACTIVATION_CODE;

  public static StringField LAST_ACTIVATION_CODE;

  public static LongField ACCESS_COUNT;

  public static DateField LAST_ACCESS_DATE;

  public static StringField REPO_ID;

  public static BlobField SIGNATURE;

  public static DateField DATE_KILLED_1;  // plus recent

  public static StringField KILLED_REPO_ID;

  public static DateField DATE_KILLED_2;

  public static DateField DATE_KILLED_3;

  public static DateField DATE_KILLED_4; // plus vieux

  @DefaultLong(0L)
  public static LongField KILLED_COUNT;

  public static StringField TRANSACTION_ID;

  public static IntegerField GROUP_ID;

  public static LongField TIME_STAMP;

  public static LongField JAR_VERSION;


  static {
    GlobTypeLoader.init(License.class);
  }

}
