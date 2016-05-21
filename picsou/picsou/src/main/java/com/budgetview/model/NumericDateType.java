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

public enum NumericDateType implements GlobConstantContainer {
  YYYY_MM_DD(0, "numericDateType.yyyymmdd", "yyyy/MM/dd", "{0}/{1}/{2}"),
  MM_DD_YYYY(1, "numericDateType.mmddyyyy", "MM/dd/yyyy", "{1}/{2}/{0}"),
  DD_MM_YYYY(2, "numericDateType.ddmmyyyy", "dd/MM/yyyy", "{2}/{1}/{0}");

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  public static StringField FORMAT;
  public static StringField MESSAGE_FORMAT;

  private int id;
  private String formatKey;
  private String format;
  private String messageFormat;

  NumericDateType(int id, String formatKey, String format, String messageFormat) {
    this.id = id;
    this.formatKey = formatKey;
    this.format = format;
    this.messageFormat = messageFormat;
  }

  static {
    GlobTypeLoader.init(NumericDateType.class, "numericDateType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(NumericDateType.TYPE,
                            value(NumericDateType.ID, id),
                            value(NumericDateType.NAME, getLabel()),
                            value(NumericDateType.FORMAT, format),
                            value(NumericDateType.MESSAGE_FORMAT, messageFormat));
  }

  public Integer getId() {
    return id;
  }

  public String getFormat() {
    return format;
  }

  public String getMessageFormat() {
    return messageFormat;
  }

  public String getLabel() {
    return Lang.get(formatKey);
  }

  public static NumericDateType get(int id) {
    switch (id) {
      case 0:
        return YYYY_MM_DD;
      case 1:
        return MM_DD_YYYY;
      case 2:
        return DD_MM_YYYY;
    }
    throw new InvalidData(id + " not associated to any NumericDateType enum value");
  }

  public static NumericDateType getDefault() {
    Locale locale = Lang.getLocale();
    if (locale.equals(Lang.FR) || locale.equals(Locale.FRANCE)) {
      return DD_MM_YYYY;
    }
    else if (locale.equals(Lang.EN) || locale.equals(Locale.ENGLISH)) {
      return MM_DD_YYYY;
    }
    else {
      return YYYY_MM_DD;
    }
  }
}
