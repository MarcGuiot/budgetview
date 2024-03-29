package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;

import static org.globsframework.model.FieldValue.value;

public enum AccountCardType implements GlobConstantContainer {
  NOT_A_CARD("NOT_A_CARD", 0),
  UNDEFINED("UNDEFINED", 1),
  DEFERRED("DEFERRED", 2),
  CREDIT("CREDIT", 3);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;

  AccountCardType(String name, int id) {
    this.name = name;
    this.id = id;
  }

  static {
    TypeLoader.init(AccountCardType.class, "accountCardType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(AccountCardType.TYPE,
                            value(AccountCardType.ID, id),
                            value(AccountCardType.NAME, getName()));
  }

  public String getName() {
    return Strings.toNiceLowerCase(name);
  }

  public String getLabel() {
    return Lang.get("accountCardType." + Strings.toNiceLowerCase(name));
  }

  public static AccountCardType get(Glob account) {
    return get(account.get(Account.CARD_TYPE));
  }

  public static AccountCardType get(Integer id) {
    if (id == null) {
      return UNDEFINED;
    }
    switch (id) {
      case 0:
        return NOT_A_CARD;
      case 1:
        return UNDEFINED;
      case 2:
        return DEFERRED;
      case 3:
        return CREDIT;
    }
    throw new ItemNotFound(id + " not associated to any AccountType enum value");
  }

  public Integer getId() {
    return id;
  }

  public org.globsframework.model.Key getKey() {
    return org.globsframework.model.Key.create(AccountCardType.TYPE, id);
  }
}