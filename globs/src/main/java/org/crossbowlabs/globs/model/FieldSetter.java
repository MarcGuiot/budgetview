package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

import java.util.Date;

public interface FieldSetter {
  void set(DoubleField field, Double value) throws ItemNotFound;

  void set(DateField field, Date value) throws ItemNotFound;

  void set(TimeStampField field, Date value) throws ItemNotFound;

  void set(IntegerField field, Integer value) throws ItemNotFound;

  void set(LinkField field, Integer value) throws ItemNotFound;

  void set(StringField field, String value) throws ItemNotFound;

  void set(BooleanField field, Boolean value) throws ItemNotFound;

  void set(LongField field, Long value) throws ItemNotFound;

  void set(BlobField field, byte[] value) throws ItemNotFound;

  void setValue(Field field, Object value) throws ItemNotFound;
}
