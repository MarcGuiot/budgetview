package org.crossbowlabs.globs.model.delta;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.ChangeSetVisitor;
import org.crossbowlabs.globs.model.MutableGlob;

public interface DeltaGlob extends MutableGlob {

  boolean isSet(Field field);

  void setState(DeltaState state);

  void visit(ChangeSetVisitor visitor) throws Exception;

  void safeVisit(ChangeSetVisitor visitor);

  void resetValues();

  DeltaState getState();

}
