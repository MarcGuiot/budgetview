package org.globsframework.wicket.model;

import org.globsframework.metamodel.Field;
import org.globsframework.model.MutableFieldValues;
import wicket.Component;
import wicket.model.AbstractModel;
import wicket.util.convert.ConversionException;
import wicket.util.convert.ConverterFactory;
import wicket.util.convert.IConverter;

import java.util.Locale;

public class FieldValueModel extends AbstractModel {

  private final MutableFieldValues values;
  private Field field;

  public FieldValueModel(Field field, MutableFieldValues values) {
    this.values = values;
    this.field = field;
  }

  public String getFieldName() {
    return field.getName();
  }

  public Object getObject(final Component component) {
    return values.getValue(field);
  }

  public void setObject(final Component component, final Object newValue) {
    IConverter converter = new ConverterFactory().newConverter(Locale.getDefault());
    Object convertedValue = null;
    try {
      convertedValue = converter.convert(newValue, field.getValueClass());
    }
    catch (ConversionException e) {

    }
    values.setValue(field, convertedValue);
  }
}
