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
import org.globsframework.utils.exceptions.InvalidData;

public enum ProfileType implements GlobConstantContainer {
  CUSTOM("CUSTOM", 0, 1, 8),
  IRREGULAR("UNKNOWN", 1, -1, 9),
  EVERY_MONTH("EVERY_MONTH", 2, -1, 1),
  TWO_MONTHS("EVERY_TWO_MONTHS", 3, 2, 2),
  THREE_MONTHS("EVERY_THREE_MONTHS", 4, 3, 3),
  SIX_MONTHS("EVERY_SIX_MONTHS", 6, 6, 5),
  ONCE_A_YEAR("ONCE_A_YEAR", 7, 12, 6),
  SINGLE_MONTH("SINGLE_MONTH", 8, -1, 7);

//   Weekly / bi-weekly: On laisse l'observed dans SeriesBudget.
//   On ne fait porter par WeeklyBudget que le montant pour
//   la semaine et le jour. Question : peut-on desactiver un WeeklyBudget ? ==> si oui il faut le representer qqpart

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private int id;
  private String name;
  private int monthStep;
  private int order;

  ProfileType(String name, int id, int monthStep, int order) {
    this.name = name.toLowerCase();
    this.id = id;
    this.monthStep = monthStep;
    this.order = order;
  }

  static {
    GlobTypeLoader.init(ProfileType.class, "profileType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ProfileType.TYPE,
                            value(ProfileType.ID, id),
                            value(ProfileType.NAME, name));
  }

  public static ProfileType get(int id) {
    switch (id) {
      case 0:
        return CUSTOM;
      case 1:
        return IRREGULAR;
      case 2:
        return EVERY_MONTH;
      case 3:
        return TWO_MONTHS;
      case 4:
        return THREE_MONTHS;
      case 6:
        return SIX_MONTHS;
      case 7:
        return ONCE_A_YEAR;
      case 8:
        return SINGLE_MONTH;
    }
    throw new InvalidData(id + " not associated to any ProfileType enum value");
  }

  public Integer getId() {
    return id;
  }

  public int getMonthStep() {
    return monthStep;
  }

  public Integer getOrder() {
    return order;
  }

  public String getLabel() {
    return Lang.get("profileType." + name);
  }
}
