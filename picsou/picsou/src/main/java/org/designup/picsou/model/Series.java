package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.utils.exceptions.InvalidData;

public class Series {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  @NamingField
  public static StringField NAME;

  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @Target(Category.class)
  public static LinkField DEFAULT_CATEGORY;

  @Target(ProfileType.class)
  public static LinkField PROFILE_TYPE;

  public static IntegerField FIRST_MONTH;

  public static IntegerField LAST_MONTH;

  public static IntegerField OCCURENCES_COUNT;

  public static IntegerField DAY;

  public static DoubleField AMOUNT;

  public static DoubleField MIN_AMOUNT;

  public static DoubleField MAX_AMOUNT;

  @DefaultBoolean(false)
  public static BooleanField JANUARY;
  @DefaultBoolean(false)
  public static BooleanField FEBRUARY;
  @DefaultBoolean(false)
  public static BooleanField MARCH;
  @DefaultBoolean(false)
  public static BooleanField APRIL;
  @DefaultBoolean(false)
  public static BooleanField MAY;
  @DefaultBoolean(false)
  public static BooleanField JUNE;
  @DefaultBoolean(false)
  public static BooleanField JULY;
  @DefaultBoolean(false)
  public static BooleanField AUGUST;
  @DefaultBoolean(false)
  public static BooleanField SEPTEMBER;
  @DefaultBoolean(false)
  public static BooleanField OCTOBER;
  @DefaultBoolean(false)
  public static BooleanField NOVEMBER;
  @DefaultBoolean(false)
  public static BooleanField DECEMBER;

  static {
    GlobTypeLoader.init(Series.class);
  }

  public static BooleanField getField(int monthId) {
    switch (Month.toMonth(monthId)) {
      case 1:
        return JANUARY;
      case 2:
        return FEBRUARY;
      case 3:
        return MARCH;
      case 4:
        return APRIL;
      case 5:
        return MAY;
      case 6:
        return JUNE;
      case 7:
        return JULY;
      case 8:
        return AUGUST;
      case 9:
        return SEPTEMBER;
      case 10:
        return OCTOBER;
      case 11:
        return NOVEMBER;
      case 12:
        return DECEMBER;
    }
    throw new InvalidData(Month.toString(monthId) + " not managed");
  }


}
