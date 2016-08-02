package com.budgetview.model;

import com.budgetview.desktop.description.Formatting;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Day {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @Key
  public static IntegerField DAY;

  static {
    GlobTypeLoader.init(Day.class, "day");
  }

  public static String getFullLabel(org.globsframework.model.Key dayKey) {
    int month = dayKey.get(Day.MONTH);
    int day = dayKey.get(Day.DAY);
    return Formatting.getFullLabel(month, day);
  }
}
