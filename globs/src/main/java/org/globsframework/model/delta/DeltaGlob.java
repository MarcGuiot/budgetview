package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Key;
import org.globsframework.model.FieldValuesWithPrevious;

public interface DeltaGlob extends FieldValuesWithPrevious {

  Key getKey();

  DeltaState getState();

  void setState(DeltaState state);

  boolean isSet(Field field);

  void setValue(Field field, Object value);

  void setValue(Field field, Object value, Object previousValue);

  void setValues(FieldValues values);

  void setPreviousValues(FieldValues values);

  FieldValues getValues();

  FieldValues getPreviousValues();

  void resetValues();

  void visit(ChangeSetVisitor visitor) throws Exception;

  void safeVisit(ChangeSetVisitor visitor);
}
