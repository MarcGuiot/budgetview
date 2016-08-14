package org.globsframework.json;

import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldValues;
import org.json.JSONWriter;

import java.io.PrintWriter;

public class JsonGlobWriter extends JSONWriter {
  private FieldValues fieldValues;

  public JsonGlobWriter(PrintWriter printWriter) {
    super(printWriter);
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

  public void field(StringField field, String key) {
    key(key);
    value(fieldValues.get(field));
  }
}
