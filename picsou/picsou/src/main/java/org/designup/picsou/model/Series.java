package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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

  public static BooleanField JANUARY;
  public static BooleanField FEBRUARY;
  public static BooleanField MARCH;
  public static BooleanField APRIL;
  public static BooleanField MAY;
  public static BooleanField JUNE;
  public static BooleanField JULY;
  public static BooleanField AUGUST;
  public static BooleanField SEPTEMBER;
  public static BooleanField OCTOBER;
  public static BooleanField NOVEMBER;
  public static BooleanField DECEMBER;

  static {
    GlobTypeLoader.init(Series.class);
  }
}
