package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public interface FieldValues extends Serializable {

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

  boolean contains(Field field);

  int size();

  void apply(Functor functor) throws Exception;

  void safeApply(Functor functor);

  /** @deprecated */
  Map<Field, Object> getMap();

  FieldValue[] toArray();

  interface Functor {
    void process(Field field, Object value) throws Exception;
  }

  FieldValues EMPTY = new FieldValues() {
    public boolean contains(Field field) {
      return false;
    }

    public int size() {
      return 0;
    }

    public void apply(Functor functor) throws Exception {
    }

    public void safeApply(Functor functor) {
    }

    public Map<Field, Object> getMap() {
      return Collections.emptyMap();
    }

    public Double get(DoubleField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Date get(DateField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Date get(TimeStampField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Integer get(IntegerField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Integer get(LinkField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public String get(StringField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Boolean get(BooleanField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Boolean get(BooleanField field, boolean defaultIfNull) {
      throw new ItemNotFound(field.getName());
    }

    public Object getValue(Field field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public Long get(LongField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public byte[] get(BlobField field) throws ItemNotFound {
      throw new ItemNotFound(field.getName());
    }

    public FieldValue[] toArray() {
      return new FieldValue[0];
    }
  };
}
