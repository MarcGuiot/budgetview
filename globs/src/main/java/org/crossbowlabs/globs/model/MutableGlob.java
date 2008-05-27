package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;

import java.util.Date;

public interface MutableGlob extends Glob {

  void set(IntegerField field, Integer value);

  void set(DoubleField field, Double value);

  void set(StringField field, String value);

  void set(DateField field, Date value);

  void set(BooleanField field, Boolean value);

  void set(BlobField field, byte[] value);

  void setObject(Field field, Object value);

  void setValues(FieldValues values);
}
