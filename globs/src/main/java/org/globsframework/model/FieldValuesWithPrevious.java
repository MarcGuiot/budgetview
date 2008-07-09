package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Date;

public interface FieldValuesWithPrevious extends FieldValues {
  Object getValue(Field field) throws ItemNotFound;

  Double get(DoubleField field) throws ItemNotFound;

  Date get(DateField field) throws ItemNotFound;

  Date get(TimeStampField field) throws ItemNotFound;

  Integer get(IntegerField field) throws ItemNotFound;

  Integer get(LinkField field) throws ItemNotFound;

  String get(StringField field) throws ItemNotFound;

  Boolean get(BooleanField field) throws ItemNotFound;

  Boolean get(BooleanField field, boolean defaultIfNull);

  Long get(LongField field) throws ItemNotFound;

  byte[] get(BlobField field) throws ItemNotFound;

  Object getPreviousValue(Field field) throws ItemNotFound;

  Double getPrevious(DoubleField field) throws ItemNotFound;

  Date getPrevious(DateField field) throws ItemNotFound;

  Date getPrevious(TimeStampField field) throws ItemNotFound;

  Integer getPrevious(IntegerField field) throws ItemNotFound;

  Integer getPrevious(LinkField field) throws ItemNotFound;

  String getPrevious(StringField field) throws ItemNotFound;

  Boolean getPrevious(BooleanField field) throws ItemNotFound;

  Boolean getPrevious(BooleanField field, boolean defaultIfNull);

  Long getPrevious(LongField field) throws ItemNotFound;

  byte[] getPrevious(BlobField field) throws ItemNotFound;

  void apply(Functor functor) throws Exception;

  void safeApply(Functor functor);

  interface Functor {
    void process(Field field, Object value, Object previousValue) throws Exception;
  }
}
