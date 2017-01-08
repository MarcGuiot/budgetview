package org.globsframework.json;

import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.json.JSONWriter;

import java.io.Writer;

public class JsonGlobWriter extends JSONWriter {
  private FieldValues fieldValues;

  public JsonGlobWriter(Writer writer) {
    super(writer);
  }

  public void setCurrentValues(FieldValues values) {
    this.fieldValues = values;
  }

  public void field(IntegerField field, String key) {
    key(key);
    value(fieldValues.get(field));
  }

  public void field(LinkField field, String key) {
    key(key);
    value(fieldValues.get(field));
  }

  public void field(DoubleField field, String key) {
    key(key);
    value(fieldValues.get(field));
  }

  public void field(DateField field, String key) {
    key(key);
    value(JsonGlobFormat.toString(fieldValues.get(field)));
  }

  public void field(StringField field, String key) {
    key(key);
    value(fieldValues.get(field));
  }

  public void field(BooleanField field, String key) {
    key(key);
    value(fieldValues.get(field));
  }

  public void value(Integer value, String key) {
    key(key);
    value(value);
  }
}
