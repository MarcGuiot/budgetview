package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;

import static org.globsframework.model.FieldValue.value;

public enum SignpostSectionType implements GlobConstantContainer {
  NOT_STARTED(0, 1),
  IMPORT(1, 2),
  CATEGORIZATION(2, 3),
  BUDGET(3, -1),
  COMPLETED(-1, -1);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  int id;
  private int nextId;

  static {
    TypeLoader.init(SignpostSectionType.class, "signpostSectionType");
  }

  SignpostSectionType(int id, int nextId) {
    this.id = id;
    this.nextId = nextId;
  }

  public int getId() {
    return id;
  }

  public org.globsframework.model.Key getKey() {
    return org.globsframework.model.Key.create(SignpostSectionType.TYPE, id);
  }

  public SignpostSectionType getNextSection() {
    return getType(nextId);
  }

  public boolean isLast() {
    return nextId < 0;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(SignpostSectionType.TYPE,
                            value(SignpostSectionType.ID, id));
  }

  public static SignpostSectionType getType(Integer sectionTypeId) {
    if (sectionTypeId == null) {
      return NOT_STARTED;
    }
    for (SignpostSectionType type : values()) {
      if (type.id == sectionTypeId) {
        return type;
      }
    }
    throw new ItemNotFound("No type found for id: " + sectionTypeId);
  }

  public boolean isCompleted(SignpostSectionType currentType) {
    return currentType == COMPLETED || currentType.id > id;
  }
}
