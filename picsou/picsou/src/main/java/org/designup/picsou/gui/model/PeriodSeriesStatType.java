package org.designup.picsou.gui.model;

import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidData;

import static org.globsframework.model.FieldValue.value;

public enum PeriodSeriesStatType implements GlobConstantContainer {
  SERIES(0, Series.TYPE),
  SERIES_GROUP(1, SeriesGroup.TYPE);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private final int id;
  private final GlobType targetType;

  static {
    GlobTypeLoader.init(PeriodSeriesStatType.class, "periodSeriesStatType");
  }

  PeriodSeriesStatType(int id, GlobType targetType) {
    this.id = id;
    this.targetType = targetType;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ProfileType.TYPE,
                            value(ProfileType.ID, id));
  }

  public Integer getId() {
    return id;
  }

  public org.globsframework.model.Key getTargetKey(Glob periodSeriesStat) {
    return org.globsframework.model.Key.create(targetType, periodSeriesStat.get(PeriodSeriesStat.TARGET));
  }

  public static PeriodSeriesStatType get(Glob periodSeriesStat) {
    return get(periodSeriesStat.get(PeriodSeriesStat.TARGET_TYPE));
  }

  public static PeriodSeriesStatType get(int id) {
    switch (id) {
      case 0:
        return SERIES;
      case 1:
        return SERIES_GROUP;
    }
    throw new InvalidData(id + " not associated to any PeriodSeriesStatType enum value");
  }
}

