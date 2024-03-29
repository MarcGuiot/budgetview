package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.GlobList;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public enum BudgetArea implements GlobConstantContainer {
  ALL("ALL", -1, false, false),
  INCOME("INCOME", 0, true, true),
  RECURRING("RECURRING", 1, false, true),
  VARIABLE("VARIABLE", 2, false, false),
  EXTRAS("EXTRAS", 4, false, false),
  TRANSFER("TRANSFER", 5, false, false),
  UNCATEGORIZED("UNCATEGORIZED", 6, false, true),
  OTHER("OTHER", 7, false, true);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;
  private boolean income;
  private boolean automatic;

  public static final BudgetArea[] INCOME_AND_EXPENSES_AREAS = {INCOME, RECURRING, VARIABLE, EXTRAS, TRANSFER};

  BudgetArea(String name, int id, boolean isIncome, final boolean automatic) {
    this.automatic = automatic;
    this.name = name;
    this.id = id;
    this.income = isIncome;
  }

  public boolean isIncome() {
    return income;
  }

  public boolean isAutomatic() {
    return automatic;
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

  public static BudgetArea get(int id) {
    switch (id) {
      case -1:
        return ALL;
      case 0:
        return INCOME;
      case 1:
        return RECURRING;
      case 2:
        return VARIABLE;
      case 4:
        return EXTRAS;
      case 5:
        return TRANSFER;
      case 6:
        return UNCATEGORIZED;
      case 7:
        return OTHER;
    }
    throw new ItemNotFound(id + " not associated to any BugdetArea enum value");
  }

  public static List<BudgetArea> getAll(Integer[] ids) {
    List<BudgetArea> result = new ArrayList<BudgetArea>();
    for (Integer id : ids) {
      result.add(get(id));
    }
    return result;
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

  public static boolean shouldInvertAmounts(BudgetArea area) {
    return !area.isIncome() && (area != BudgetArea.UNCATEGORIZED);
  }

  public static Set<org.globsframework.model.Key> getKeys(Set<BudgetArea> budgetAreas) {
    Set<org.globsframework.model.Key> keys = new HashSet<org.globsframework.model.Key>();
    for (BudgetArea budgetArea : budgetAreas) {
      keys.add(budgetArea.getKey());
    }
    return keys;
  }
}
