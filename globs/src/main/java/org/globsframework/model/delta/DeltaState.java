package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.exceptions.InvalidState;

public interface DeltaState {
  void processCreation(DeltaGlob glob, FieldValues values);

  void processUpdate(DeltaGlob glob, Field field, Object value, Object previousValue);

  void processDeletion(DeltaGlob glob, FieldValues values);

  void visit(DeltaGlob glob, ChangeSetVisitor visitor) throws Exception;

  DeltaState UNCHANGED = new DeltaState() {
    public void processCreation(DeltaGlob delta, FieldValues values) {
      delta.setState(CREATED);
      delta.setValues(values);
    }

    public void processUpdate(DeltaGlob delta, Field field, Object value, Object previousValue) {
      delta.setState(UPDATED);
      delta.setValue(field, value);
    }

    public void processDeletion(DeltaGlob delta, FieldValues values) {
      delta.setState(DELETED);
      delta.setValues(values);
    }

    public void visit(DeltaGlob glob, ChangeSetVisitor visitor) throws Exception {
    }

    public String toString() {
      return "unchanged";
    }
  };

  DeltaState CREATED = new DeltaState() {
    public void processCreation(DeltaGlob glob, FieldValues values) {
      throw new InvalidState("Object " + glob.getKey() + " already exists");
    }

    public void processUpdate(DeltaGlob glob, Field field, Object value, Object previousValue) {
      glob.setValue(field, value, previousValue);
    }

    public void processDeletion(DeltaGlob glob, FieldValues values) {
      glob.setState(UNCHANGED);
    }

    public void visit(DeltaGlob glob, ChangeSetVisitor visitor) throws Exception {
      visitor.visitCreation(glob.getKey(), glob.getValues());
    }

    public String toString() {
      return "created";
    }
  };

  DeltaState UPDATED = new DeltaState() {
    public void processCreation(DeltaGlob delta, FieldValues values) {
      throw new InvalidState("Object " + delta.getKey() + " already exists");
    }

    public void processUpdate(DeltaGlob delta, Field field, Object value, Object previousValue) {
      delta.setValue(field, value, previousValue);
    }

    public void processDeletion(DeltaGlob delta, FieldValues values) {
      delta.setState(DELETED);
      delta.setValues(values);
    }

    public void visit(DeltaGlob delta, ChangeSetVisitor visitor) throws Exception {
      visitor.visitUpdate(delta.getKey(), delta);
    }

    public String toString() {
      return "updated";
    }
  };

  DeltaState DELETED = new DeltaState() {
    public void processCreation(DeltaGlob glob, FieldValues values) {
      glob.resetValues();
      glob.setState(UPDATED);
      glob.setValues(values);
    }

    public void processUpdate(DeltaGlob delta, Field field, Object value, Object previousValue) {
      throw new InvalidState("Object " + delta.getKey() + " was deleted and cannot be updated");
    }

    public void processDeletion(DeltaGlob glob, FieldValues values) {
      throw new InvalidState("Object " + glob.getKey() + " was already deleted");
    }

    public void visit(DeltaGlob glob, ChangeSetVisitor visitor) throws Exception {
      visitor.visitDeletion(glob.getKey(), glob.getValues());
    }

    public String toString() {
      return "deleted";
    }
  };
}
