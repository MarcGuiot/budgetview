package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.ChangeSetVisitor;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Key;

public class DefaultChangeSetVisitor implements ChangeSetVisitor {
  public void visitCreation(Key key, FieldValues values) throws Exception {
  }

  public void visitUpdate(Key key, FieldValues values) throws Exception {
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
  }
}
