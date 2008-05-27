package org.crossbowlabs.globs.xml;

import java.text.*;
import java.util.Date;
import java.util.Locale;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;

public class FieldConverter {
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0#",
                                                                        new DecimalFormatSymbols(Locale.US));
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
  private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  private DateFormat dateFormat = DATE_FORMAT;

  public String toString(Field field, Object value) {
    if (value == null) {
      return "(null)";
    }
    FieldStringifierVisitor visitor = new FieldStringifierVisitor(value);
    field.safeVisit(visitor);
    return visitor.getStringValue();
  }

  public Object toObject(Field field, String stringValue) {
    try {
      XmlStringifierVisitor visitor = new XmlStringifierVisitor(stringValue);
      field.safeVisit(visitor);
      return visitor.getValue();
    }
    catch (Exception e) {
      throw new InvalidParameter("'" + stringValue + "' is not a proper value for field '" + field.getName() +
                                 "' in type '" + field.getGlobType().getName() + "'", e);
    }
  }

  public void setDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

  public String toString(Date date) {
    return dateFormat.format(date);
  }

  public Date toDate(String value) {
    try {
      return dateFormat.parse(value);
    }
    catch (ParseException e) {
      throw new InvalidParameter("'" + value + "' is not a properly formatted date", e);
    }
  }

  public Date toTimestamp(String value) {
    try {
      return TIMESTAMP_FORMAT.parse(value);
    }
    catch (ParseException e) {
      throw new InvalidParameter("'" + value + "' is not a properly formatted timestamp", e);
    }
  }

  private class XmlStringifierVisitor implements FieldVisitor {
    private final String stringValue;
    private Object value;

    public XmlStringifierVisitor(String stringValue) {
      this.stringValue = stringValue;
    }

    public void visitDate(DateField field) throws Exception {
      value = toDate(stringValue);
    }

    public void visitBoolean(BooleanField field) throws Exception {
      value = stringValue.equalsIgnoreCase("true");
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      value = toTimestamp(stringValue);
    }

    public void visitBlob(BlobField field) throws Exception {
      value = stringValue.getBytes();
    }

    public void visitString(StringField field) throws Exception {
      value = stringValue;
    }

    public void visitDouble(DoubleField field) throws Exception {
      value = Double.parseDouble(stringValue);
    }

    public void visitInteger(IntegerField field) throws Exception {
      value = Integer.parseInt(stringValue);
    }

    public void visitLong(LongField field) throws Exception {
      value = Long.parseLong(stringValue);
    }

    public void visitLink(LinkField field) throws Exception {
      visitInteger(field);
    }

    public Object getValue() {
      return value;
    }
  }

  private class FieldStringifierVisitor implements FieldVisitor {
    private Object value;
    private String stringValue;

    private FieldStringifierVisitor(Object value) {
      this.value = value;
    }

    public String getStringValue() {
      return stringValue;
    }

    public void visitDate(DateField field) throws Exception {
      stringValue = dateFormat.format(value);
    }

    public void visitBoolean(BooleanField field) throws Exception {
      if (((Boolean)value)) {
        stringValue = "true";
      }
      else {
        stringValue = "false";
      }
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      stringValue = TIMESTAMP_FORMAT.format(value);
    }

    public void visitBlob(BlobField field) throws Exception {
      stringValue = new String((byte[])value);
    }

    public void visitString(StringField field) throws Exception {
      stringValue = (String)value;
    }

    public void visitDouble(DoubleField field) throws Exception {
      if (((Double)value) == 0.0) {
        stringValue = "0.0";
      }
      stringValue = DECIMAL_FORMAT.format(value);
    }

    public void visitInteger(IntegerField field) throws Exception {
      stringValue = Integer.toString(((Integer)value));
    }

    public void visitLong(LongField field) throws Exception {
      stringValue = Long.toString(((Long)value));
    }

    public void visitLink(LinkField field) throws Exception {
      visitInteger(field);
    }
  }
}
