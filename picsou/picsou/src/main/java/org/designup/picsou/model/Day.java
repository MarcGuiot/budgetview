package org.designup.picsou.model;

import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;

import java.util.Calendar;

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
    return Lang.get("day.full", Integer.toString(Month.toYear(month)), Month.getFullMonthLabel(month), day);
  }
}
