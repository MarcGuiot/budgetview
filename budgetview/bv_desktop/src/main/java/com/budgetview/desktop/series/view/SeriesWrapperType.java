package com.budgetview.desktop.series.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;

import static org.globsframework.model.FieldValue.value;

public enum SeriesWrapperType implements GlobConstantContainer {
  SERIES(1),
  SERIES_GROUP(2),
  SUB_SERIES(3),
  BUDGET_AREA(4),
  SUMMARY(5);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;

  private SeriesWrapperType(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(SeriesWrapperType.class, "SeriesWrapperType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(SeriesWrapperType.TYPE, value(SeriesWrapperType.ID, id));
  }

  public Integer getId() {
    return id;
  }

  public org.globsframework.model.Key getKey() {
    return org.globsframework.model.Key.create(SeriesWrapperType.TYPE, id);
  }

  public static SeriesWrapperType get(Glob wrapper) {
    return get(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static SeriesWrapperType get(int id) {
    switch (id) {
      case 1:
        return SERIES;
      case 2:
        return SERIES_GROUP;
      case 3:
        return SUB_SERIES;
      case 4:
        return BUDGET_AREA;
      case 5:
        return SUMMARY;
    }
    throw new ItemNotFound(id + " is not associated to any SeriesWrapperType enum value");
  }

  public boolean isOfType(Glob wrapper) {
    return (wrapper != null) && (id == wrapper.get(SeriesWrapper.ITEM_TYPE));
  }
}