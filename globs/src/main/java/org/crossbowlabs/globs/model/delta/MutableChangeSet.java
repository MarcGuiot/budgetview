package org.crossbowlabs.globs.model.delta;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;

public interface MutableChangeSet extends ChangeSet {
  void processCreation(Key globKey, FieldValues values);

  void processUpdate(Key key, Field field, Object newValue);

  void processUpdate(Key key, FieldValues values);
  
  void processDeletion(Key key, FieldValues values);

  void merge(ChangeSet other) throws InvalidState;
}
