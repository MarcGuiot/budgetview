package org.globsframework.model.format;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.TimeStampField;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Strings;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class GlobTreePrinter {
  private StringBuilder builder = new StringBuilder();
  private int indent = 0;
  public static final DecimalFormat DECIMAL_FORMAT =
    new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

  public void writeIndented(String type,
                            FieldValues values,
                            Field idField,
                            Field... otherFields) {
    builder.append(Strings.repeat("  ", indent));
    builder.append(type).append(" ");
    builder.append(idField.getName()).append(": ").append(value(values, idField)).append('\n');
    for (Field field : otherFields) {
      builder.append(Strings.repeat("  ", indent + 1));
      builder.append(field.getName()).append(": ").append(value(values, field)).append('\n');
    }
  }

  public void writeFlat(String type,
                        FieldValues values,
                        Field... fields) {
    builder.append(Strings.repeat("  ", indent));
    builder.append(type).append(" ");
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      builder.append(field.getName()).append(": ").append(value(values, field));
      if (i < fields.length - 1) {
        builder.append(" - ");
      }
    }
    builder.append('\n');
  }

  public void enter() {
    indent++;
  }

  public void leave() {
    indent--;
  }

  public String toString() {
    return builder.toString();
  }

  private String value(FieldValues values, Field field) {
    Object value = values.getValue(field);
    if (value == null) {
      return "";
    }
    if ((field instanceof DateField)) {
      return Dates.toString((Date) value);
    }
    if ((field instanceof TimeStampField)) {
      return Dates.toTimestampString((Date) value);
    }
    if ((field instanceof DoubleField)) {
      return DECIMAL_FORMAT.format(value);
    }
    return value.toString();
  }
}
