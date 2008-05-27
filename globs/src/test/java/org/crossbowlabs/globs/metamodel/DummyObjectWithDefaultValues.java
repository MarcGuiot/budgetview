package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.annotations.*;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.sqlstreams.annotations.AutoIncrement;

public class DummyObjectWithDefaultValues {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @DefaultInteger(7)
  public static IntegerField INTEGER;

  @DefaultLong(5l)
  public static LongField LONG;

  @DefaultDouble(3.14159265)
  public static DoubleField DOUBLE;

  @DefaultBoolean(true)
  public static BooleanField BOOLEAN;

  @DefaultDate()
  public static DateField DATE;

  @DefaultDate()
  public static TimeStampField TIMESTAMP;

  static {
    GlobTypeLoader.init(DummyObjectWithDefaultValues.class);
  }
}
