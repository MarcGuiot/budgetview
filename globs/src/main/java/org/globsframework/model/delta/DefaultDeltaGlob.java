package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.*;
import org.globsframework.model.impl.AbstractFieldValuesWithPrevious;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.Date;

public class DefaultDeltaGlob extends AbstractFieldValuesWithPrevious implements DeltaGlob {

  private Key key;
  private Object[] values;
  private Object[] previousValues;
  private DeltaState state = DeltaState.UNCHANGED;

  // TODO: to be made serialization-proof
  public static Object UNSET_VALUE = new Object() {
    public String toString() {
      return "<deltaGlob.unset>";
    }
  };

  public DefaultDeltaGlob(Key key) {
    this.key = key;
    setValues(key);
    resetValues();
  }

  protected Object doGet(Field field) {
    return values[field.getIndex()];
  }

  protected Object doGetPrevious(Field field) {
    return previousValues[field.getIndex()];
  }

  public Key getKey() {
    return key;
  }

  public void setValues(FieldValues values) {
    values.apply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {

      }
    });
  }

  public boolean isModified() {
    return state != DeltaState.UNCHANGED;
  }

  public boolean isCreated() {
    return state == DeltaState.CREATED;
  }

  public boolean isDeleted() {
    return state == DeltaState.DELETED;
  }

  public boolean isUpdated() {
    return state == DeltaState.UPDATED;
  }

  public boolean isUpdated(Field field) {
    return (state == DeltaState.UPDATED) && (!UNSET_VALUE.equals(values[field.getIndex()]));
  }

  public boolean isSet(Field field) {
    return doGet(field) != UNSET_VALUE;
  }

  public boolean contains(Field field) {
    return values[field.getIndex()] != UNSET_VALUE;
  }

  public int size() {
    int count = 0;
    for (Object value : values) {
      if (value != UNSET_VALUE) {
        count++;
      }
    }
    return count;
  }

  public void setState(DeltaState state) {
    this.state = state;
  }

  public void setValue(Field field, Object value) {
    values[field.getIndex()] = value;
  }

  public FieldValues getValues() {
    return new FieldValuesFromArray(key.getGlobType(), values);
  }

  public void apply(Functor functor) throws Exception {
    for (Field field : key.getGlobType().getFields()) {
      Object value = values[field.getIndex()];
      if (value != UNSET_VALUE) {
        functor.process(field, value, previousValues[field.getIndex()]);
      }
    }
  }

  public void safeApply(Functor functor) {
    try {
      apply(functor);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void resetValues() {
    for (Field field : key.getGlobType().getFields()) {
      if (!field.isKeyField()) {
        values[field.getIndex()] = UNSET_VALUE;
      }
    }
  }

  public DeltaState getState() {
    return state;
  }

  public void processCreation(FieldValues values) {
    state.processCreation(this, values);
  }

  public void processUpdate(Field field, Object value, Object previousValue) {
    state.processUpdate(this, field, value, previousValue);
  }

  public void processDeletion(FieldValues values) {
    state.processDeletion(this, values);
  }

  public void visit(ChangeSetVisitor visitor) throws Exception {
    state.visit(this, visitor);
  }

  public void safeVisit(ChangeSetVisitor visitor) {
    try {
      state.visit(this, visitor);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  static private class FieldValuesFromArray implements FieldValues {
    private GlobType type;
    private Object[] values;

    public FieldValuesFromArray(GlobType type, Object[] values) {
      this.type = type;
      this.values = values;
    }

    public Object getValue(Field field) throws ItemNotFound {
      Object value = values[field.getIndex()];
      if (value == UNSET_VALUE) {
        throw new ItemNotFound(field.getName() + " not set.");
      }
      return value;
    }

    public Double get(DoubleField field) throws ItemNotFound {
      return (Double)getValue(field);
    }

    public Date get(DateField field) throws ItemNotFound {
      return (Date)getValue(field);
    }

    public Date get(TimeStampField field) throws ItemNotFound {
      return (Date)getValue(field);
    }

    public Integer get(IntegerField field) throws ItemNotFound {
      return (Integer)getValue(field);
    }

    public Integer get(LinkField field) throws ItemNotFound {
      return (Integer)getValue(field);
    }

    public String get(StringField field) throws ItemNotFound {
      return (String)getValue(field);
    }

    public Boolean get(BooleanField field) throws ItemNotFound {
      return (Boolean)getValue(field);
    }

    public Boolean get(BooleanField field, boolean defaultIfNull) {
      Boolean value = (Boolean)getValue(field);
      if (value == null) {
        return defaultIfNull;
      }
      return value;
    }

    public Long get(LongField field) throws ItemNotFound {
      return (Long)getValue(field);
    }

    public byte[] get(BlobField field) throws ItemNotFound {
      return (byte[])getValue(field);
    }

    public boolean contains(Field field) {
      if (field.isKeyField()) {
        return false;
      }
      return values[field.getIndex()] != UNSET_VALUE;
    }

    public int size() {
      int count = -type.getKeyFields().size();
      for (Object value : values) {
        if (value != UNSET_VALUE) {
          count++;
        }
      }
      return count;
    }

    public void apply(Functor functor) throws Exception {
      for (Field field : type.getFields()) {
        Object value = values[field.getIndex()];
        if (value != UNSET_VALUE && !field.isKeyField()) {
          functor.process(field, value);
        }
      }
    }

    public void safeApply(Functor functor) {
      try {
        for (Field field : type.getFields()) {
          Object value = values[field.getIndex()];
          if (value != UNSET_VALUE && !field.isKeyField()) {
            functor.process(field, value);
          }
        }
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public FieldValue[] toArray() {
      FieldValue[] fieldValues = new FieldValue[size()];
      int i = 0;
      for (Field field : type.getFields()) {
        Object value = values[field.getIndex()];
        if (value != UNSET_VALUE && !field.isKeyField()) {
          fieldValues[i] = new FieldValue(field, value);
          i++;
        }
      }
      return fieldValues;
    }
  }
}
