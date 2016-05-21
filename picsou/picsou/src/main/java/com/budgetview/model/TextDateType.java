package com.budgetview.model;

import com.budgetview.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidData;

import java.util.Locale;

import static org.globsframework.model.FieldValue.value;

public enum TextDateType implements GlobConstantContainer {
  DAY_MONTH_YEAR(0, "textDateType.dayMonthYear", "{2} {1} {0}"),
  MONTH_DAY_YEAR(1, "textDateType.monthDayYear", "{1} {2}, {0}");

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  public static StringField FORMAT;

  private int id;
  private String formatKey;
  private String format;

  TextDateType(int id, String formatKey, String format) {
    this.id = id;
    this.formatKey = formatKey;
    this.format = format;
  }

  static {
    GlobTypeLoader.init(TextDateType.class, "textDateType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(TextDateType.TYPE,
                            value(TextDateType.ID, id),
                            value(TextDateType.NAME, getLabel()),
                            value(TextDateType.FORMAT, format));
  }

  public static TextDateType get(int id) {
    switch (id) {
      case 0:
        return DAY_MONTH_YEAR;
      case 1:
        return MONTH_DAY_YEAR;
    }
    throw new InvalidData(id + " not associated to any TextDateType enum value");
  }

  public Integer getId() {
    return id;
  }

  public String getLabel() {
    return Lang.get(formatKey);
  }

  public String getFormat() {
    return format;
  }

  public static TextDateType getDefault() {
    Locale locale = Lang.getLocale();
    if (locale.equals(Lang.FR) || locale.equals(Locale.FRANCE)) {
      return DAY_MONTH_YEAR;
    }
    else {
      return MONTH_DAY_YEAR;
    }
  }
}
