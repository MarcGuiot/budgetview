package org.designup.picsou.gui.series.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;

public enum SeriesWrapperType implements GlobConstantContainer {
  SERIES(1, 1),
  SUB_SERIES(2, 2),
  BUDGET_AREA(3, 0),
  SUMMARY(4, 0);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;
  private int level;

  private SeriesWrapperType(int id, int level) {
    this.id = id;
    this.level = level;
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
  
  public int getLevel() {
    return level;
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
        return SUB_SERIES;
      case 3:
        return BUDGET_AREA;
      case 4:
        return SUMMARY;
    }
    throw new ItemNotFound(id + " is not associated to any SeriesWrapperType enum value");
  }

  public boolean isOfType(Glob wrapper) {
    return (wrapper != null) && (id == wrapper.get(SeriesWrapper.ITEM_TYPE));
  }
}