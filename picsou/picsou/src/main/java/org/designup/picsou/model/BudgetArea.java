package org.designup.picsou.model;

import org.designup.picsou.utils.Lang;
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

public enum BudgetArea implements GlobConstantContainer {
  ALL("ALL", -1, false, false),
  INCOME("INCOME", 0, true, true),
  RECURRING("RECURRING", 1, false, false),
  ENVELOPES("ENVELOPES", 2, false, false),
  SPECIAL("SPECIAL", 4, false, false),
  SAVINGS("SAVINGS", 5, false, true),
  UNCATEGORIZED("UNCATEGORIZED", 6, false, false);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;
  private boolean income;
  private boolean overrunAllowed;

  public static final BudgetArea[] INCOME_AREAS = {INCOME};
  public static final BudgetArea[] EXPENSES_AREAS = {RECURRING, ENVELOPES, SPECIAL, SAVINGS};
  public static final BudgetArea[] INCOME_AND_EXPENSES_AREAS = {INCOME, RECURRING, ENVELOPES, SPECIAL, SAVINGS};

  BudgetArea(String name, int id, boolean isIncome, boolean overrunAllowed) {
    this.name = name;
    this.id = id;
    this.income = isIncome;
    this.overrunAllowed = overrunAllowed;
  }

  public boolean isIncome() {
    return income;
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

  public double getMultiplier() {
    return isIncome() ? 1 : -1;
  }
}
