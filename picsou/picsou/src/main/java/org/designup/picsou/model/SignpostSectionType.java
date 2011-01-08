package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Utils;
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
    GlobTypeLoader.init(SignpostSectionType.class, "signpostSectionType");
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
    if (currentType == COMPLETED) {
      return true;
    }
    return currentType.id > id;
  }

  public static boolean isCompleted(GlobRepository repository) {
    Glob status = repository.find(SignpostStatus.KEY);
    return (status != null && Utils.equal(status.get(SignpostStatus.CURRENT_SECTION), COMPLETED.id));
  }
}
