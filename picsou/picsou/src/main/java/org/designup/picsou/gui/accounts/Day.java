package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Day {
  public static GlobType TYPE;

  @Key @NamingField
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(Day.class, "day");
  }

}