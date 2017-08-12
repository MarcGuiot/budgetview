package com.budgetview.model;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;

public class Day {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @Key
  public static IntegerField DAY;

  static {
    TypeLoader.init(Day.class, "day");
  }

  public static String getFullLabel(org.globsframework.model.Key dayKey) {
    int month = dayKey.get(Day.MONTH);
    int day = dayKey.get(Day.DAY);
    return Formatting.getFullLabel(month, day);
  }
}
