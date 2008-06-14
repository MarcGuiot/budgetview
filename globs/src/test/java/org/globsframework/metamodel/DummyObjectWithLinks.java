package org.globsframework.metamodel;

import org.globsframework.metamodel.annotations.ContainmentLink;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class DummyObjectWithLinks {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField TARGET_ID_1;
  public static IntegerField TARGET_ID_2;

  public static IntegerField PARENT_ID;
  public static IntegerField SIBLING_ID;

  public static Link COMPOSITE_LINK;

  @ContainmentLink
  public static Link PARENT_LINK;

  public static Link SIBLING_LINK;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(DummyObjectWithLinks.class);
    loader.defineLink(COMPOSITE_LINK)
      .add(TARGET_ID_1, DummyObjectWithCompositeKey.ID1)
      .add(TARGET_ID_2, DummyObjectWithCompositeKey.ID2);
    loader.defineLink(PARENT_LINK)
      .add(PARENT_ID, DummyObject.ID);
    loader.defineLink(SIBLING_LINK)
      .add(SIBLING_ID, DummyObjectWithLinks.ID);
  }
}
