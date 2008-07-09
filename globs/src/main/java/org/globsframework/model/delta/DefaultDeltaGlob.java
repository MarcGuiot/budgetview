package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.impl.AbstractFieldValuesWithPrevious;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

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
    int fieldCount = key.getGlobType().getFieldCount();
    this.values = new Object[fieldCount];
    this.previousValues = new Object[fieldCount];
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

  public void setValue(Field field, Object value) {
    values[field.getIndex()] = value;
  }

  public void setValue(Field field, Object value, Object previousValue) {
    int index = field.getIndex();
    values[index] = value;
    previousValues[index] = previousValue;
  }

  public void setValues(FieldValues values) {
    values.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        setValue(field, value);
      }
    });
  }

  public void setPreviousValues(FieldValues values) {
    values.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        previousValues[field.getIndex()] = value;
      }
    });
  }

  public FieldValues getValues() {
    return this;
  }

  public FieldValues getPreviousValues() {
    return new DeltaFieldValuesFromArray(key.getGlobType(), previousValues);

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

  public void setState(DeltaState state) {
    this.state = state;
  }

  public void resetValues() {
    for (Field field : key.getGlobType().getFields()) {
      if (!field.isKeyField()) {
        values[field.getIndex()] = UNSET_VALUE;
      }
      previousValues[field.getIndex()] = null;
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

  public Object getValue(Field field) throws ItemNotFound {
    Object value = values[field.getIndex()];
    if (value == UNSET_VALUE) {
      throw new ItemNotFound(field.getName() + " not set.");
    }
    return value;
  }

  public boolean contains(Field field) {
    if (field.isKeyField()) {
      return false;
    }
    return values[field.getIndex()] != UNSET_VALUE;
  }

  public int size() {
    int count = -key.getGlobType().getKeyFields().size();
    for (Object value : values) {
      if (value != UNSET_VALUE) {
        count++;
      }
    }
    return count;
  }

  public void apply(FieldValues.Functor functor) throws Exception {
    for (Field field : key.getGlobType().getFields()) {
      Object value = values[field.getIndex()];
      if ((value != UNSET_VALUE) && !field.isKeyField()) {
        functor.process(field, value);
      }
    }
  }

  public void safeApply(FieldValues.Functor functor) {
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

  public void apply(FieldValuesWithPrevious.Functor functor) throws Exception {
    for (Field field : key.getGlobType().getFields()) {
      final int index = field.getIndex();
      Object value = values[index];
      if ((value != UNSET_VALUE) && !field.isKeyField()) {
        functor.process(field, value, previousValues[index]);
      }
    }
  }

  public void safeApply(FieldValuesWithPrevious.Functor functor) {
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

  public FieldValue[] toArray() {
    FieldValue[] fieldValues = new FieldValue[size()];
    int i = 0;
    for (Field field : key.getGlobType().getFields()) {
      Object value = values[field.getIndex()];
      if (value != UNSET_VALUE && !field.isKeyField()) {
        fieldValues[i] = new FieldValue(field, value);
        i++;
      }
    }
    return fieldValues;
  }

  public GlobType getType() {
    return key.getGlobType();
  }
}
