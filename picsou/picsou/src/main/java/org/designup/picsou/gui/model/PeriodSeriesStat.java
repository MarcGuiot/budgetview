package org.designup.picsou.gui.model;

import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class PeriodSeriesStat {
  public static GlobType TYPE;

  @Key
  @Target(Series.class)
  public static LinkField SERIES;

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  public static DoubleField PLANNED_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PAST_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField FUTURE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField PAST_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField FUTURE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField ABS_SUM_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PREVIOUS_SUMMARY_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField NEW_SUMMARY_AMOUNT;

  public static IntegerField PREVIOUS_SUMMARY_MONTH;
  public static IntegerField NEW_SUMMARY_MONTH;

  @DefaultBoolean(false)
  public static BooleanField ACTIVE;

  @DefaultBoolean(false)
  public static BooleanField TO_SET;

  static {
    GlobTypeLoader.init(PeriodSeriesStat.class);
  }
}
