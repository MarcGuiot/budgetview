package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;

import java.util.Date;

public interface MutableGlob extends Glob {

  void set(IntegerField field, Integer value);

  void set(DoubleField field, Double value);

  void set(StringField field, String value);

  void set(DateField field, Date value);

  void set(BooleanField field, Boolean value);

  void set(BlobField field, byte[] value);

  Object setObject(Field field, Object value);

  void setValues(FieldValues values);
}
