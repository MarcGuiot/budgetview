package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;

public enum BudgetArea implements GlobConstantContainer {
  INCOME("INCOME", 0, true, false),
  RECURRING_EXPENSES("RECURRING_EXPENSES", 1, false, false),
  EXPENSES_ENVELOPE("EXPENSES_ENVELOPE", 2, false, true),
  OCCASIONAL_EXPENSES("OCCASIONAL_EXPENSES", 3, false, true),
  PROJECTS("PROJECTS", 4, false, true),
  SAVINGS("SAVINGS", 5, false, false),
  UNCATEGORIZED("UNCATEGORIZED", 6, false, true);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;
  private boolean income;
  private boolean multiCategories;

  BudgetArea(String name, int id, boolean isIncome, boolean multiCategories) {
    this.name = name;
    this.id = id;
    this.income = isIncome;
    this.multiCategories = multiCategories;
  }

  public boolean isIncome() {
    return income;
  }

  public boolean isMultiCategories() {
    return multiCategories;
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
      case 0:
        return INCOME;
      case 1:
        return RECURRING_EXPENSES;
      case 2:
        return EXPENSES_ENVELOPE;
      case 3:
        return OCCASIONAL_EXPENSES;
      case 4:
        return PROJECTS;
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
}
