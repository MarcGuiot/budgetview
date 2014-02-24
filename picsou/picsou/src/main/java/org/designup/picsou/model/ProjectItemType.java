package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidData;

import static org.globsframework.model.FieldValue.value;

public enum ProjectItemType implements GlobConstantContainer {
  EXPENSE(0),
  TRANSFER(1);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;

  ProjectItemType(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(ProjectItemType.class, "projectItemType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ProfileType.TYPE,
                            value(ProfileType.ID, id));
  }

  public static ProjectItemType get(Glob item) {
    return get(item.get(ProjectItem.ITEM_TYPE));
  }

  public static ProjectItemType get(int id) {
    switch (id) {
      case 0:
        return EXPENSE;
      case 1:
        return TRANSFER;
    }
    throw new InvalidData(id + " not associated to any ProjectItemType enum value");
  }

  public Integer getId() {
    return id;
  }

  public static GlobMatcher isExpenses() {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item != null && Utils.equal(EXPENSE.id, item.get(ProjectItem.ITEM_TYPE));
      }
    };
  }

  public static GlobMatcher isTransfer() {
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item != null && Utils.equal(TRANSFER.id, item.get(ProjectItem.ITEM_TYPE));
      }
    };
  }
}
