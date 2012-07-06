package org.designup.picsou.model;

import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;

import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;

public enum AccountUpdateMode implements GlobConstantContainer {
  AUTOMATIC("AUTOMATIC", 1),
  MANUAL("MANUAL", 2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;

  AccountUpdateMode(String name, int id) {
    this.name = name;
    this.id = id;
  }

  static {
    GlobTypeLoader.init(AccountUpdateMode.class, "accountUpdateMode");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(AccountUpdateMode.TYPE,
                            value(AccountUpdateMode.ID, id),
                            value(AccountUpdateMode.NAME, getName()));
  }

  public String getName() {
    return Lang.get("account.updateMode." + name.toLowerCase());
  }

  public static AccountUpdateMode get(int id) {
    switch (id) {
      case 1:
        return AUTOMATIC;
      case 2:
        return MANUAL;
    }
    throw new ItemNotFound(id + " not associated to any AccountUpdateMode enum value");
  }

  public Integer getId() {
    return id;
  }

  public org.globsframework.model.Key getKey() {
    return org.globsframework.model.Key.create(AccountUpdateMode.TYPE, id);
  }

  public static boolean isAutomatic(Glob account) {
    return Utils.equal(AUTOMATIC.id, account.get(Account.UPDATE_MODE));
  }
}