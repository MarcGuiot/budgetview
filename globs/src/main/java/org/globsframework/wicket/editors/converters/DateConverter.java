package org.globsframework.wicket.editors.converters;

import org.globsframework.wicket.GlobSession;
import wicket.Session;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

public class DateConverter implements IConverter {
  private Locale locale;

  protected Class getTargetType() {
    return Date.class;
  }

  public Object convert(Object value, Class targetClass) {
    if (value == null) {
      return null;
    }

    GlobSession session = (GlobSession)Session.get();
    DateFormat dateFormat = session.getDescriptionService().getFormats().getDateFormat();
    if (targetClass.equals(String.class)) {
      return dateFormat.format(value);
    }
    else if (targetClass.equals(Date.class)) {
      String string = (String)value;
      return parse(dateFormat, string);
    }
    throw new RuntimeException("Unexpected type: " + value.getClass().getName());
  }

  private Object parse(DateFormat format, String stringValue) {
    final ParsePosition position = new ParsePosition(0);
    final Date result = (Date)format.parseObject(stringValue, position);
    if (position.getIndex() != stringValue.length()) {
      throw new ConversionException("Cannot parse '" + stringValue + "' using format " + format)
        .setSourceValue(stringValue).setTargetType(Date.class).setLocale(locale);
    }
    return result;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }
}
