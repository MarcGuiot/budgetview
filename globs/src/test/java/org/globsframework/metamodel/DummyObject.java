package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.ContainmentLink;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.index.UniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class DummyObject {

  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  public static DoubleField VALUE;
  public static BooleanField PRESENT;
  public static DateField DATE;
  public static TimeStampField TIMESTAMP;
  public static BlobField PASSWORD;

  @Target(DummyObject.class)
  public static LinkField LINK;

  @ContainmentLink
  public static Link CHILD_DUMMY_OBJECT_WITH_LINK;

  public static UniqueIndex NAME_INDEX;
  public static NotUniqueIndex DATE_INDEX;


  static {
    GlobTypeLoader loader = GlobTypeLoader.init(DummyObject.class);
    loader.defineLink(CHILD_DUMMY_OBJECT_WITH_LINK)
      .add(ID, DummyObjectWithLinks.PARENT_ID);
    loader.defineUniqueIndex(NAME_INDEX, NAME);
    loader.defineNotUniqueIndex(DATE_INDEX, DATE);
  }
}