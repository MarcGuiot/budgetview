package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Date;

public interface FieldSetter {
  FieldSetter set(DoubleField field, Double value) throws ItemNotFound;

  FieldSetter set(DateField field, Date value) throws ItemNotFound;

  FieldSetter set(TimeStampField field, Date value) throws ItemNotFound;

  FieldSetter set(IntegerField field, Integer value) throws ItemNotFound;

  FieldSetter set(LinkField field, Integer value) throws ItemNotFound;

  FieldSetter set(StringField field, String value) throws ItemNotFound;

  FieldSetter set(BooleanField field, Boolean value) throws ItemNotFound;

  FieldSetter set(LongField field, Long value) throws ItemNotFound;

  FieldSetter set(BlobField field, byte[] value) throws ItemNotFound;

  FieldSetter setValue(Field field, Object value) throws ItemNotFound;
}
