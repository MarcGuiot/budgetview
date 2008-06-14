package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;

import java.util.List;

public interface ChangeSet {

  boolean containsChanges(GlobType type);

  boolean containsCreationsOrDeletions(GlobType type);

  boolean containsUpdates(Field field);

  boolean containsChanges(Key key);

  boolean isEmpty();

  GlobType[] getChangedTypes();

  int getChangeCount();

  int getChangeCount(GlobType type);

  List<Key> getCreated(GlobType type);

  List<Key> getUpdated(GlobType type);

  List<Key> getDeleted(GlobType type);

  void visit(ChangeSetVisitor visitor) throws Exception;

  void safeVisit(ChangeSetVisitor visitor);

  void visit(GlobType type, ChangeSetVisitor visitor) throws Exception;

  void safeVisit(GlobType type, ChangeSetVisitor visitor);

  void visit(Key key, ChangeSetVisitor visitor) throws Exception;

  void safeVisit(Key key, ChangeSetVisitor visitor);
}
