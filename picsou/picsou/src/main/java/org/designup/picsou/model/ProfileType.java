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

public enum ProfileType implements GlobConstantContainer {
  MONTHLY(0),
  UNKNOWN(1),
  CREDIT(2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private int id;

  ProfileType(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(ProfileType.class);
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ProfileType.TYPE,
                            value(ProfileType.ID, id),
                            value(ProfileType.NAME, Strings.toNiceLowerCase(name())));
  }

  public static ProfileType get(int id) {
    switch (id) {
      case 0:
        return MONTHLY;
      case 1:
        return UNKNOWN;
      case 2:
        return CREDIT;
    }
    throw new InvalidData(id + " not associated to any Profile enum value");
  }

  public Integer getId() {
    return id;
  }

}