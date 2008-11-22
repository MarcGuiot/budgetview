package org.designup.picsou.model;

import org.designup.picsou.utils.Lang;
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

public enum AccountType implements GlobConstantContainer {
  MAIN("MAIN", 1),
  SAVINGS("SAVINGS", 2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;

  AccountType(String name, int id) {
    this.name = name;
    this.id = id;
  }

  static public Integer[] getAccountType() {
    return new Integer[]{1, 2, 3};
  }

  static {
    GlobTypeLoader.init(AccountType.class, "accountType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(AccountType.TYPE,
                            value(AccountType.ID, id),
                            value(AccountType.NAME, getName()));
  }

  public String getName() {
    return Strings.toNiceLowerCase(name);
  }

  public String getLabel() {
    return Lang.get("budgetArea." + getName());
  }

  public static AccountType get(int id) {
    switch (id) {
      case 1:
        return MAIN;
      case 2:
        return SAVINGS;
    }
    throw new ItemNotFound(id + " not associated to any AccountType enum value");
  }

  public Integer getId() {
    return id;
  }

  public org.globsframework.model.Key getKey() {
    return org.globsframework.model.Key.create(AccountType.TYPE, id);
  }
}