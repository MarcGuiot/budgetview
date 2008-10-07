package org.designup.picsou.gui.model;

import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class PeriodSeriesStat {
  public static GlobType TYPE;

  @Key
  @Target(Series.class)
  public static LinkField SERIES;

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PLANNED_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField ABS_SUM_AMOUNT;

  static {
    GlobTypeLoader.init(PeriodSeriesStat.class);
  }
}