package org.globsframework.wicket.editors.converters;

import org.apache.wicket.Session;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.globsframework.wicket.GlobSession;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class DoubleConverter implements IConverter {
  private Locale locale;

  public DoubleConverter() {
  }

  public Object convertToObject(String value, Locale locale) {
    if (value == null) {
      return null;
    }

    GlobSession session = (GlobSession)Session.get();
    DecimalFormat decimalFormat = session.getDescriptionService().getFormats().getDecimalFormat();
    return parse(decimalFormat, value);
  }

  public String convertToString(Object value, Locale locale) {
    if (value == null) {
      return null;
    }

    GlobSession session = (GlobSession)Session.get();
    DecimalFormat decimalFormat = session.getDescriptionService().getFormats().getDecimalFormat();
    return decimalFormat.format(value);
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