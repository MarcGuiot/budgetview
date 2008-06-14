package org.globsframework.wicket.editors.converters;

import org.globsframework.wicket.GlobSession;
import wicket.Session;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class DoubleConverter implements IConverter {
  private Locale locale;

  protected Class getTargetType() {
    return Double.class;
  }

  public Object convert(Object value, Class targetClass) {
    if (value == null) {
      return null;
    }

    GlobSession session = (GlobSession)Session.get();
    DecimalFormat decimalFormat = session.getDescriptionService().getFormats().getDecimalFormat();
    if (targetClass.equals(String.class)) {
      return decimalFormat.format(value);
    }
    else if (targetClass.equals(Double.class)) {
      String string = (String)value;
      return parse(decimalFormat, string);
    }
    throw new RuntimeException("Unexpected type: " + value.getClass().getName());
  }

  private Object parse(DecimalFormat format, String stringValue) {
    final ParsePosition position = new ParsePosition(0);
    final Number result = (Number)format.parseObject(stringValue, position);
    if (position.getIndex() != stringValue.length()) {
      throw new ConversionException("Cannot parse '" + stringValue + "' using format " + format)
        .setSourceValue(stringValue).setTargetType(Double.class).setLocale(locale);
    }
    if (result == null) {
      return null;
    }
    return result.doubleValue();
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }
}