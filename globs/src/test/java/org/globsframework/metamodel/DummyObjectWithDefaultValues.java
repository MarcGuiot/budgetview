package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

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

  @Target(DummyObject.class)
  @DefaultInteger(1)
  public static LinkField LINK;

  @DefaultString("Hello")
  public static StringField STRING;

  static {
    GlobTypeLoader.init(DummyObjectWithDefaultValues.class);
  }
}
