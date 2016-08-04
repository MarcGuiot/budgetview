package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class DummyObjectWithNamedFields {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @Name("named_integer")
  public static IntegerField INTEGER;

  @Name("named_long")
  public static LongField LONG;

  @Name("named_double")
  public static DoubleField DOUBLE;

  @Name("named_boolean")
  public static BooleanField BOOLEAN;

  @Name("named_date")
  public static DateField DATE;

  @Name("named_timestamp")
  public static TimeStampField TIMESTAMP;

  @Name("named_link")
  @Target(DummyObject.class)
  public static LinkField LINK;

  @Name("named_string")
  public static StringField STRING;

  static {
    GlobTypeLoader.init(DummyObjectWithNamedFields.class);
  }
}
