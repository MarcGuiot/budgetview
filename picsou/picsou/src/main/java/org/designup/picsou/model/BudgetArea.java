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
    GlobTypeLoader.init(BudgetArea.class);
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(BudgetArea.TYPE,
                            value(BudgetArea.ID, id),
                            value(BudgetArea.NAME, Strings.toNiceLowerCase(name())));
  }

  public Integer getId() {
    return id;
  }
}
