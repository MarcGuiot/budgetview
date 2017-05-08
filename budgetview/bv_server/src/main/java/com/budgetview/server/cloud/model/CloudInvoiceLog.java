package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class CloudInvoiceLog {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @Target(CloudUser.class)
  public static LinkField USER;

  public static StringField EMAIL;

  public static DateField DATE;

  public static DoubleField AMOUNT;

  public static StringField RECEIPT_NUMBER;

  public static BooleanField EMAIL_SENT;

  static {
    GlobTypeLoader.init(CloudInvoiceLog.class, "cloudInvoiceLog");
  }
}
