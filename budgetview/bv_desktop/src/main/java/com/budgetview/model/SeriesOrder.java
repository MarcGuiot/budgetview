package com.budgetview.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;

import static org.globsframework.model.FieldValue.value;

public enum SeriesOrder implements GlobConstantContainer {
  DEFAULT(0),
  NAME_ASCENDING(1),
  NAME_DESCENDING(2),
  REAL_AMOUNT_ASCENDING(3),
  REAL_AMOUNT_DESCENDING(4),
  PLANNED_AMOUNT_ASCENDING(5),
  PLANNED_AMOUNT_DESCENDING(6);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;

  SeriesOrder(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(SeriesOrder.class, "seriesOrder");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(SeriesOrder.TYPE, value(SeriesOrder.ID, id));
  }

  public boolean isAscending() {
    return (id % 2) == 0;
  }

  public static SeriesOrder get(int id) {
    for (SeriesOrder seriesOrder : values()) {
      if (seriesOrder.id == id) {
        return seriesOrder;
      }
    }
    throw new ItemNotFound("No SeriesOrder found for id: " + id);
  }

  public Integer getId() {
    return id;
  }
}
