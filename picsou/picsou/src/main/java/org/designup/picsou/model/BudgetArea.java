package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobList;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.designup.picsou.utils.Lang;

public enum BudgetArea implements GlobConstantContainer {
  ALL("ALL", -1, false, false, false),
  INCOME("INCOME", 0, true, false, true),
  RECURRING("RECURRING", 1, false, false, false),
  ENVELOPES("ENVELOPES", 2, false, true, false),
  OCCASIONAL("OCCASIONAL", 3, false, true, false),
  SPECIAL("SPECIAL", 4, false, true, false),
  SAVINGS("SAVINGS", 5, false, false, true),
  UNCATEGORIZED("UNCATEGORIZED", 6, false, true, false);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;
  private boolean income;
  private boolean multiCategories;
  private boolean overrunAllowed;

  BudgetArea(String name, int id, boolean isIncome, boolean multiCategories, boolean overrunAllowed) {
    this.name = name;
    this.id = id;
    this.income = isIncome;
    this.multiCategories = multiCategories;
    this.overrunAllowed = overrunAllowed;
  }

  public boolean isIncome() {
    return income;
  }

  public boolean isMultiCategories() {
    return multiCategories;
  }

  public boolean isOverrunAllowed() {
    return overrunAllowed;
  }

  static {
    GlobTypeLoader.init(BudgetArea.class, "budgetArea");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(BudgetArea.TYPE,
                            value(BudgetArea.ID, id),
                            value(BudgetArea.NAME, getName()));
  }

  public String getName() {
    return Strings.toNiceLowerCase(name);
  }

  public String getLabel() {
    return Lang.get("budgetArea." + getName());
  }

  public static BudgetArea get(int id) {
    switch (id) {
      case -1:
        return ALL;
      case 0:
        return INCOME;
      case 1:
        return RECURRING;
      case 2:
        return ENVELOPES;
      case 3:
        return OCCASIONAL;
      case 4:
        return SPECIAL;
      case 5:
        return SAVINGS;
      case 6:
        return UNCATEGORIZED;
    }
    throw new ItemNotFound(id + " not associated to any BugdetArea enum value");
  }

  public Integer getId() {
    return id;
  }

  public org.globsframework.model.Key getKey() {
    return org.globsframework.model.Key.create(BudgetArea.TYPE, id);
  }

  public static GlobList getGlobs(ReadOnlyGlobRepository repository, BudgetArea... budgetAreas) {
    GlobList result = new GlobList();
    for (BudgetArea budgetArea : budgetAreas) {
      result.add(repository.get(budgetArea.getKey()));
    }
    return result;
  }
}
