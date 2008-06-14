package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.utils.exceptions.*;

public interface GlobRepository extends ReadOnlyGlobRepository {

  Glob create(GlobType type, FieldValue... values)
    throws MissingInfo, ItemAlreadyExists;

  Glob create(Key key, FieldValue... values)
    throws ItemAlreadyExists;

  Glob findOrCreate(Key key, FieldValue... defaultValues)
    throws MissingInfo;

  void update(Key key, Field field, Object newValue)
    throws ItemNotFound;

  void update(Key key, FieldValue... values);

  void setTarget(Key source, Link link, Key target)
    throws ItemNotFound;

  void delete(Key key)
    throws ItemNotFound, OperationDenied;

  void delete(GlobList list)
    throws OperationDenied;

  void deleteAll(GlobType... types)
    throws OperationDenied;

  /**
   * Replaces all the globs of given types with those of the provided list.
   * If no types are given, all globs will be replaced.
   * A {@link ChangeSetListener#globsReset(GlobRepository, java.util.List)} notification
   * is sent after the reset is performed.
   */
  void reset(GlobList newGlobs, GlobType... changedTypes);

  void apply(ChangeSet changeSet) throws InvalidParameter;

  void addTrigger(ChangeSetListener listener);

  void removeTrigger(ChangeSetListener listener);

  void addChangeListener(ChangeSetListener listener);

  void removeChangeListener(ChangeSetListener listener);

  void enterBulkDispatchingMode();

  void completeBulkDispatchingMode();

  /**
   * @deprecated Unused - at least remove the "count" parameter (caching should be done at the
   *             GlobIdGenerator level
   */
  Integer getNextId(IntegerField field, int count);

  GlobIdGenerator getIdGenerator();
}
