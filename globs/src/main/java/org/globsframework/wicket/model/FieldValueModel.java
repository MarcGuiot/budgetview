package org.globsframework.wicket.model;

import org.apache.wicket.model.Model;
import org.globsframework.metamodel.Field;
import org.globsframework.model.MutableFieldValues;

public class FieldValueModel extends Model {
  private final MutableFieldValues values;
  private Field field;

  public FieldValueModel(Field field, MutableFieldValues values) {
    this.values = values;
    this.field = field;
  }

  public String getFieldName() {
    return field.getName();
  }

  public Object getObject() {
    return values.getValue(field);
  }

  public void setObject(final Object newValue) {
//    IConverter converter = new ConverterLocator().getConverter(field.getValueClass());
//    Object convertedValue = null;
//    try {
//      convertedValue = converter.convertToObject((String)newValue, Locale.getDefault());
//    }
//    catch (ConversionException e) {
//
//    }
    values.setValue(field, newValue);
  }
}
