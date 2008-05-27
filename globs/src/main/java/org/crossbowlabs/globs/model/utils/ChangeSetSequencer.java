package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeDependencies;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.MultiMap;

public class ChangeSetSequencer {

  public static void process(ChangeSet changeSet, GlobModel model, ChangeSetVisitor visitor)
    throws Exception {

    GlobTypeDependencies dependencies = model.getDependencies();

    final MultiMap<GlobType, Change> creations = new MultiMap<GlobType, Change>();
    final MultiMap<GlobType, Change> updates = new MultiMap<GlobType, Change>();
    final MultiMap<GlobType, Change> deletions = new MultiMap<GlobType, Change>();

    dispatchChanges(changeSet, creations, updates, deletions, dependencies);

    for (GlobType type : dependencies.getCreationSequence()) {
      for (Change change : creations.get(type)) {
        visitor.visitCreation(change.getKey(), change.getValues());
      }
    }

    for (GlobType type : dependencies.getUpdateSequence()) {
      for (Change change : updates.get(type)) {
        visitor.visitUpdate(change.getKey(), change.getValues());
      }
    }

    for (GlobType type : dependencies.getDeletionSequence()) {
      for (Change change : deletions.get(type)) {
        visitor.visitDeletion(change.getKey(), change.getValues());
      }
    }
  }

  private static void dispatchChanges(ChangeSet changeSet,
                                      final MultiMap<GlobType, Change> creations,
                                      final MultiMap<GlobType, Change> updates,
                                      final MultiMap<GlobType, Change> deletions,
                                      final GlobTypeDependencies dependencies) {
    changeSet.safeVisit(new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (!dependencies.needsPostUpdate(key.getGlobType())) {
          creations.put(key.getGlobType(), new Change(key, values));
          return;
        }
        final FieldValuesBuilder builderForCreation = FieldValuesBuilder.init();
        final FieldValuesBuilder builderForUpdate = FieldValuesBuilder.init();
        values.apply(new FieldValues.Functor() {
          public void process(Field field, Object value) throws Exception {
            if (field.isRequired()) {
              builderForCreation.setObject(field, value);
            }
            else {
              builderForUpdate.setObject(field, value);
            }
          }
        });
        creations.put(key.getGlobType(), new Change(key, builderForCreation.get()));
        FieldValues valuesForUpdate = builderForUpdate.get();
        if (valuesForUpdate.size() > 0) {
          updates.put(key.getGlobType(), new Change(key, valuesForUpdate));
        }
      }

      public void visitUpdate(Key key, FieldValues values) throws Exception {
        updates.put(key.getGlobType(), new Change(key, values));
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        deletions.put(key.getGlobType(), new Change(key, values));
      }
    });
  }

  private static class Change {

    private Key key;
    private FieldValues values;

    private Change(Key key, FieldValues values) {
      this.key = key;
      this.values = values;
    }

    public Key getKey() {
      return key;
    }

    public FieldValues getValues() {
      return values;
    }
  }
}
