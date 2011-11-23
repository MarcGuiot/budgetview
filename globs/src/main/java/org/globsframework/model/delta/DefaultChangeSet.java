package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.xml.XmlChangeSetWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultChangeSet implements MutableChangeSet {
  private MapOfMaps<GlobType, Key, DefaultDeltaGlob> deltaGlobsByKey = new MapOfMaps<GlobType, Key, DefaultDeltaGlob>();

  public void processCreation(Key key, FieldValues values) {
    DefaultDeltaGlob delta = getGlob(key);
    delta.processCreation(values);
    removeIfUnchanged(delta);
  }

  public void processCreation(GlobType type, FieldValues values) {
    processCreation(KeyBuilder.createFromValues(type, values), FieldValuesBuilder.removeKeyFields(values));
  }

  public void processUpdate(Key key, Field field, Object newValue, Object previousValue) {
    DefaultDeltaGlob delta = getGlob(key);
    delta.processUpdate(field, newValue, previousValue);
    removeIfUnchanged(delta);
  }

  public void processUpdate(Key key, FieldValuesWithPrevious values) {
    final DefaultDeltaGlob delta = getGlob(key);
    values.safeApply(new FieldValuesWithPrevious.Functor() {
      public void process(Field field, Object value, Object previousValue) throws Exception {
        delta.processUpdate(field, value, previousValue);
        removeIfUnchanged(delta);
      }
    });
  }

  public void processDeletion(Key key, FieldValues values) {
    DefaultDeltaGlob delta = getGlob(key);
    delta.processDeletion(values);
    removeIfUnchanged(delta);
  }

  private void removeIfUnchanged(DefaultDeltaGlob delta) {
    if (!delta.isModified()) {
      deltaGlobsByKey.remove(delta.getKey().getGlobType(), delta.getKey());
    }
  }

  private DefaultDeltaGlob getGlob(Key key) {
    DefaultDeltaGlob glob = deltaGlobsByKey.get(key.getGlobType(), key);
    if (glob == null) {
      glob = new DefaultDeltaGlob(key);
      deltaGlobsByKey.put(key.getGlobType(), key, glob);
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
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.get(type).values()) {
      deltaGlob.visit(visitor);
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
    DefaultDeltaGlob deltaGlob = deltaGlobsByKey.get(key.getGlobType(), key);
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
    return !deltaGlobsByKey.get(type).isEmpty();
  }

  public GlobType[] getChangedTypes() {
    Set<GlobType> result = new HashSet<GlobType>();
    for (GlobType globType : deltaGlobsByKey.keys()) {
      if (!deltaGlobsByKey.get(globType).isEmpty()) {
        result.add(globType);
      }
    }
    return result.toArray(new GlobType[result.size()]);
  }

  public int getChangeCount() {
    return deltaGlobsByKey.size();
  }

  public int getChangeCount(GlobType type) {
    return deltaGlobsByKey.get(type).size();
  }

  public Set<Key> getCreated(GlobType type) {
    Set<Key> result = new HashSet<Key>();
    for (Map.Entry entry : deltaGlobsByKey.get(type).entrySet()) {
      DefaultDeltaGlob delta = (DefaultDeltaGlob)entry.getValue();
      if (delta.isCreated()) {
        result.add((Key)entry.getKey());
      }
    }
    return result;
  }

  public Set<Key> getUpdated(GlobType type) {
    Set<Key> result = new HashSet<Key>();
    for (DefaultDeltaGlob delta : deltaGlobsByKey.get(type).values()) {
      if (delta.isUpdated()) {
        result.add(delta.getKey());
      }
    }
    return result;
  }

  public Set<Key> getCreatedOrUpdated(GlobType type) {
    Set<Key> result = new HashSet<Key>();
    for (DefaultDeltaGlob delta : deltaGlobsByKey.get(type).values()) {
      if (delta.isCreated() || delta.isUpdated()) {
        result.add(delta.getKey());
      }
    }
    return result;
  }

  public Set<Key> getUpdated(Field field) {
    Set<Key> result = new HashSet<Key>();
    for (DefaultDeltaGlob delta : deltaGlobsByKey.get(field.getGlobType()).values()) {
      if (delta.isUpdated(field)) {
        result.add(delta.getKey());
      }
    }
    return result;
  }

  public Set<Key> getDeleted(GlobType type) {
    Set<Key> result = new HashSet<Key>();
    for (DefaultDeltaGlob delta : deltaGlobsByKey.get(type).values()) {
      if (delta.isDeleted()) {
        result.add(delta.getKey());
      }
    }
    return result;
  }

  public boolean isCreated(Key key) {
    DefaultDeltaGlob defaultDeltaGlob = deltaGlobsByKey.get(key.getGlobType(), key);
    return defaultDeltaGlob != null && defaultDeltaGlob.isCreated();
  }

  public boolean isDeleted(Key key) {
    DefaultDeltaGlob defaultDeltaGlob = deltaGlobsByKey.get(key.getGlobType(), key);
    return defaultDeltaGlob != null && defaultDeltaGlob.isDeleted();
  }

  public FieldValues getPreviousValue(Key key) {
    Map<Key, DefaultDeltaGlob> keyDefaultDeltaGlobMap = deltaGlobsByKey.get(key.getGlobType());
    if (keyDefaultDeltaGlobMap != null) {
      DefaultDeltaGlob defaultDeltaGlob = keyDefaultDeltaGlobMap.get(key);
      return defaultDeltaGlob.getPreviousValues();
    }
    return null;
  }

  public boolean containsCreationsOrDeletions(GlobType type) {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.get(type).values()) {
      if (deltaGlob.isCreated() || deltaGlob.isDeleted()) {
        return true;
      }
    }
    return false;
  }

  public boolean containsUpdates(Field field) {
    for (DefaultDeltaGlob deltaGlob : deltaGlobsByKey.get(field.getGlobType()).values()) {
      if (deltaGlob.isUpdated(field)) {
        return true;
      }
    }
    return false;
  }

  public boolean containsChanges(Key key) {
    return deltaGlobsByKey.containsKey(key.getGlobType(), key);
  }

  public boolean containsChanges(Key key, Field... fields) {
    DefaultDeltaGlob glob = deltaGlobsByKey.get(key.getGlobType(), key);
    if (glob == null) {
      return false;
    }
    if (glob.isCreated() || glob.isDeleted()) {
      return true;
    }
    for (Field field : fields) {
      if (glob.isUpdated(field)) {
        return true;
      }
    }
    return false;
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

      public void visitUpdate(final Key key, FieldValuesWithPrevious values) throws Exception {
        values.apply(new FieldValuesWithPrevious.Functor() {
          public void process(Field field, Object value, Object previousValue) throws IOException {
            processUpdate(key, field, value, previousValue);
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

  public void clear(Collection<GlobType> globTypes) {
    for (GlobType type : globTypes) {
      deltaGlobsByKey.removeAll(type);
    }
  }

  public ChangeSet reverse() {
    DefaultChangeSet result = new DefaultChangeSet();
    for (DefaultDeltaGlob delta : deltaGlobsByKey.values()) {
      DefaultDeltaGlob reverseDelta = delta.reverse();
      result.deltaGlobsByKey.put(reverseDelta.getType(), reverseDelta.getKey(), reverseDelta);
    }
    return result;
  }
}