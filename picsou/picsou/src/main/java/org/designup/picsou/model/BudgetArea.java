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
import org.globsframework.utils.exceptions.InvalidData;

public enum BudgetArea implements GlobConstantContainer {
  INCOME(0),
  RECURRING_EXPENSES(1),
  EXPENSES_ENVELOPE(2),
  OCCASIONAL_EXPENSES(3);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private int id;

  BudgetArea(int id) {
    this.id = id;
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
    return Strings.toNiceLowerCase(name());
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
    }
    throw new InvalidData(id + " not associated to any BugdetArea enum value");
  }

  public Integer getId() {
    return id;
  }
}
