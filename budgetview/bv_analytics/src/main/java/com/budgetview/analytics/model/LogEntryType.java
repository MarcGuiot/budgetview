package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum LogEntryType implements GlobConstantContainer {
  NEW_USER(1),
  KNOWN_USER(2),
  LICENCE_CHECK(3),
  DIFFERENT_CODE(4),
  PURCHASE(5);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;

  LogEntryType(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(LogEntryType.class, "commandType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(LogEntryType.TYPE,
                            value(LogEntryType.ID, id));
  }

  public Integer getId() {
    return id;
  }

  public static LogEntryType get(Integer id) {
    switch (id) {
      case 1:
        return NEW_USER;
      case 2:
        return KNOWN_USER;
      case 3:
        return LICENCE_CHECK;
      case 4:
        return DIFFERENT_CODE;
      case 5:
        return PURCHASE;
    }
    throw new InvalidParameter("No entry type found for value: " + id);
  }
}
