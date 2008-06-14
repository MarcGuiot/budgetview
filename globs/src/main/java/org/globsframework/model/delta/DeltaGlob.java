package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.MutableGlob;

public interface DeltaGlob extends MutableGlob {

  boolean isSet(Field field);

  void setState(DeltaState state);

  void visit(ChangeSetVisitor visitor) throws Exception;

  void safeVisit(ChangeSetVisitor visitor);

  void resetValues();

  DeltaState getState();

}
