package com.budgetview.desktop.model;

import com.budgetview.model.Month;
import com.budgetview.model.SubSeries;
import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;

public class SubSeriesStat {
  public static GlobType TYPE;

  @Key @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  @Key @Target(Month.class)
  public static LinkField MONTH;

  @DefaultDouble(0.00)
  public static DoubleField ACTUAL_AMOUNT;

  static {
    TypeLoader.init(SubSeriesStat.class, "subSeriesStat");
  }

  public static org.globsframework.model.Key createKey(Integer subSeriesId, Integer month) {
    return org.globsframework.model.Key.create(SUB_SERIES, subSeriesId,
                                               MONTH, month);
  }
}
