package org.crossbowlabs.globs.model.delta;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;
import org.crossbowlabs.globs.xml.XmlChangeSetWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class DefaultChangeSet implements MutableChangeSet {
  private Map<Key, DefaultDeltaGlob> deltaGlobsByKey = new HashMap<Key, DefaultDeltaGlob>();

  public void processCreation(Key key, FieldValues values) {
    DefaultDeltaGlob glob = getGlob(key);
    glob.processCreation(values);
  }

  public void processCreation(GlobType type, FieldValues values) {
    processCreation(KeyBuilder.createFromValues(type, values), FieldValuesBuilder.removeKeyFields(values));
  }

  public void processUpdate(Key key, Field field, Object newValue) {
    DefaultDeltaGlob glob = getGlob(key);
    glob.processUpdate(field, newValue);
  }

  public void processUpdate(Key key, FieldValues values) {
    final DefaultDeltaGlob glob = getGlob(key);
    values.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        glob.processUpdate(field, value);
      }
    });
  }

  public void processDeletion(Key key, FieldValues values) {
    DefaultDeltaGlob glob = getGlob(key);
    glob.processDeletion(values);
    if (!glob.isModified()) {
      deltaGlobsByKey.remove(key);
    }
  }

  private DefaultDeltaGlob getGlob(Key key) {
    DefaultDeltaGlob glob = deltaGlobsByKey.get(key);
    if (glob == null) {
      glob = new DefaultDeltaGlob(key);
      deltaGlobsByKey.put(key, glob);
    }
    return glob;
  }

  public void visit(ChangeSetVisitor visitor) throws Exception {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      deltaGlob.visit(visitor);
    }
  }

  public void safeVisit(ChangeSetVisitor visitor) {
    try {
      visit(visitor);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void visit(GlobType type, ChangeSetVisitor visitor) throws Exception {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      if (deltaGlob.getType().equals(type)) {
        deltaGlob.visit(visitor);
      }
    }
  }

  public void safeVisit(GlobType type, ChangeSetVisitor visitor) {
    try {
      visit(type, visitor);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void visit(Key key, ChangeSetVisitor visitor) throws Exception {
    DefaultDeltaGlob deltaGlob = deltaGlobsByKey.get(key);
    if (deltaGlob != null) {
      deltaGlob.visit(visitor);
    }
  }

  public void safeVisit(Key key, ChangeSetVisitor visitor) {
    try {
      visit(key, visitor);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public boolean containsChanges(GlobType type) {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      if (deltaGlob.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  public GlobType[] getChangedTypes() {
    Set<GlobType> result = new HashSet<GlobType>();
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      result.add(deltaGlob.getType());
    }
    return result.toArray(new GlobType[result.size()]);
  }

  public int getChangeCount() {
    return deltaGlobsByKey.size();
  }

  public int getChangeCount(GlobType type) {
    int count = 0;
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      if (deltaGlob.getType().equals(type)) {
        count++;
      }
    }
    return count;
  }

  public List<Key> getCreated(GlobType type) {
    List<Key> result = new ArrayList<Key>();
    for (Map.Entry entry : deltaGlobsByKey.entrySet()) {
      DefaultDeltaGlob delta = (DefaultDeltaGlob)entry.getValue();
      if (delta.getType().equals(type) && delta.isCreated()) {
        result.add((Key)entry.getKey());
      }
    }
    return result;
  }

  public List<Key> getUpdated(GlobType type) {
    List<Key> result = new ArrayList<Key>();
    for (Map.Entry entry : deltaGlobsByKey.entrySet()) {
      DefaultDeltaGlob delta = (DefaultDeltaGlob)entry.getValue();
      if (delta.getType().equals(type) && delta.isUpdated()) {
        result.add((Key)entry.getKey());
      }
    }
    return result;
  }

  public List<Key> getDeleted(GlobType type) {
    List<Key> result = new ArrayList<Key>();
    for (Map.Entry entry : deltaGlobsByKey.entrySet()) {
      DefaultDeltaGlob delta = (DefaultDeltaGlob)entry.getValue();
      if (delta.getType().equals(type) && delta.isDeleted()) {
        result.add((Key)entry.getKey());
      }
    }
    return result;
  }

  public boolean containsCreationsAndDeletions(GlobType type) {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      if (deltaGlob.getType().equals(type) &&
          (deltaGlob.isCreated() || deltaGlob.isDeleted())) {
        return true;
      }
    }
    return false;
  }

  public boolean containsUpdates(Field field) {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.values()) {
      if (deltaGlob.getType().equals(field.getGlobType()) && deltaGlob.isUpdated(field)) {
        return true;
      }
    }
    return false;
  }

  public boolean containsChanges(Key key) {
    return deltaGlobsByKey.containsKey(key);
  }

  public boolean isEmpty() {
    return deltaGlobsByKey.isEmpty();
  }

  public int size() {
    return deltaGlobsByKey.size();
  }

  public void merge(ChangeSet other) throws InvalidState {
    other.safeVisit(new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        processCreation(key, values);
      }

      public void visitUpdate(final Key key, FieldValues values) throws Exception {
        values.apply(new FieldValues.Functor() {
          public void process(Field field, Object value) throws IOException {
            processUpdate(key, field, value);
          }
        });
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        processDeletion(key, values);
      }
    });
  }

  public String toString() {
    try {
      StringWriter writer = new StringWriter();
      XmlChangeSetWriter.prettyWrite(this, writer);
      return writer.toString();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}