package org.crossbowlabs.globs.model.delta;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.impl.AbstractMutableGlob;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

public class DefaultDeltaGlob extends AbstractMutableGlob implements DeltaGlob {

  private DeltaState state = DeltaState.UNCHANGED;

  // TODO: to be made serialization-proof
  public static Object UNSET_VALUE = new Object() {
    public String toString() {
      return "<deltaGlob.unset>";
    }
  };

  public DefaultDeltaGlob(Key key) {
    super(key.getGlobType());
    setValues(key);
    resetValues();
  }

  private DefaultDeltaGlob(GlobType type, Object[] values) {
    super(type, values);
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

  public void setObject(Field field, Object value) {
    values[field.getIndex()] = value;
  }

  public FieldValues getValues(boolean includeKeyFields) {
    FieldValuesBuilder builder = FieldValuesBuilder.init();
    for (Field field : type.getFields()) {
      if (includeKeyFields || !field.isKeyField()) {
        Object value = values[field.getIndex()];
        if (value != UNSET_VALUE) {
          builder.setObject(field, value);
        }
      }
    }
    return builder.get();
  }

  public void resetValues() {
    for (Field field : type.getFields()) {
      if (!getType().isKeyField(field)) {
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

  public void processUpdate(Field field, Object value) {
    state.processUpdate(this, field, value);
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

  public Glob duplicate() {
    return new DefaultDeltaGlob(type, duplicateValues());
  }
}
