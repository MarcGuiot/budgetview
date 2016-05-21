package com.budgetview.gui.model;

import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum   SeriesType implements GlobConstantContainer {
  SERIES(0, Series.TYPE),
  SERIES_GROUP(1, SeriesGroup.TYPE);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private final int id;
  private final GlobType targetType;

  static {
    GlobTypeLoader.init(SeriesType.class, "seriesType");
  }

  SeriesType(int id, GlobType targetType) {
    this.id = id;
    this.targetType = targetType;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(SeriesType.TYPE,
                            value(SeriesType.ID, id));
  }

  public Integer getId() {
    return id;
  }

  public GlobType getTargetType() {
    return targetType;
  }

  public static SeriesType get(int id) {
    switch (id) {
      case 0:
        return SERIES;
      case 1:
        return SERIES_GROUP;
    }
    throw new InvalidData(id + " not associated to any SeriesType enum value");
  }

  public static SeriesType get(Glob target) {
    if (Series.TYPE.equals(target.getType())) {
      return SeriesType.SERIES;
    }
    if (SeriesGroup.TYPE.equals(target.getType())) {
      return SeriesType.SERIES_GROUP;
    }
    throw new InvalidParameter("Unexpected type: " + target);
  }
}

