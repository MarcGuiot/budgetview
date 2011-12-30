package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class LogEntry {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField DATE;

  @Target(LogEntryType.class) @Required
  public static LinkField ENTRY_TYPE;

  public static StringField REPO_ID;
  public static StringField EMAIL;
  public static StringField IP;

  @Target(User.class)
  public static LinkField USER;

  static {
    GlobTypeLoader.init(LogEntry.class);
  }

}
