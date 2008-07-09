package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Key;
import org.globsframework.model.FieldValuesWithPrevious;

public interface DeltaGlob extends FieldValuesWithPrevious {

  void setObject(Field field, Object value);

  boolean isSet(Field field);

  void setState(DeltaState state);

  void visit(ChangeSetVisitor visitor) throws Exception;

  void safeVisit(ChangeSetVisitor visitor);

  void resetValues();

  DeltaState getState();

  void setValues(FieldValues values);

  Key getKey();

  FieldValues getValues();
}
