package org.designup.picsou.gui.model;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.SubSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SubSeriesStat {
  public static GlobType TYPE;

  @Key @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  @Key @Target(Month.class)
  public static LinkField MONTH;

  @DefaultDouble(0.00)
  public static DoubleField AMOUNT;

  static {
    GlobTypeLoader.init(SubSeriesStat.class);
  }

  public static org.globsframework.model.Key createKey(Integer subSeriesId, Integer month) {
    return org.globsframework.model.Key.create(SUB_SERIES, subSeriesId,
                                               MONTH, month);
  }
}
