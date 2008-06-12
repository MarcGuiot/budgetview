package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.index.Index;
import org.crossbowlabs.globs.metamodel.index.MultiFieldIndex;
import org.crossbowlabs.globs.metamodel.links.FieldMappingFunctor;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.delta.DefaultChangeSet;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.model.indexing.IndexManager;
import org.crossbowlabs.globs.model.indexing.IndexSource;
import org.crossbowlabs.globs.model.indexing.IndexTables;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import org.crossbowlabs.globs.utils.MapOfMaps;
import org.crossbowlabs.globs.utils.Pair;
import org.crossbowlabs.globs.utils.Utils;
import org.crossbowlabs.globs.utils.exceptions.*;
import org.crossbowlabs.globs.xml.XmlGlobWriter;

import java.io.StringWriter;
import java.util.*;

public class DefaultGlobRepository implements GlobRepository, IndexSource {
  private MapOfMaps<GlobType, Key, Glob> globs = new MapOfMaps<GlobType, Key, Glob>();
  private List<ChangeSetListener> changeListeners = new ArrayList<ChangeSetListener>();
  private List<ChangeSetListener> triggers = new ArrayList<ChangeSetListener>();
  private int bulkDispatchingModeLevel;
  private MutableChangeSet changeSetToDispatch = new DefaultChangeSet();
  private GlobIdGenerator idGenerator = GlobIdGenerator.NONE;

  private IndexManager indexManager;

  public DefaultGlobRepository() {
    indexManager = new IndexManager(this);
  }

  public DefaultGlobRepository(GlobIdGenerator idGenerator) {
    this();
    this.idGenerator = idGenerator;
  }

  public Iterable<Glob> getGlobs(GlobType globType) {
    return globs.values(globType);
  }

  public GlobList getAll(GlobType... types) {
    if (types.length == 0) {
      return new GlobList(globs.values());
    }
    GlobList result = new GlobList();
    for (GlobType type : types) {
      result.addAll(globs.values(type));
    }
    return result;
  }

  public Glob findLinkTarget(Glob source, Link link) {
    if (source == null) {
      return null;
    }

    // Optimization
    if (link instanceof LinkField) {
      Integer value = source.get((LinkField)link);
      if (value == null) {
        return null;
      }
      Key targetKey = Key.create(link.getTargetType(), value);
      return find(targetKey);
    }

    FieldValues values = source.getTargetValues(link);
    for (Glob target : getAll(link.getTargetType())) {
      if (target.matches(values)) {
        return target;
      }
    }

    return null;
  }

  public GlobList findLinkedTo(Glob target, Link link) {
    if (link instanceof LinkField) {
      LinkField linkField = (LinkField)link;
      IntegerField targetKeyField = linkField.getTargetKeyField();
      Integer id = target.get(targetKeyField);
      return getAll(link.getSourceType(), GlobMatchers.fieldEquals((LinkField)link, id));
    }

    GlobList result = new GlobList();
    for (Glob glob : getAll(link.getSourceType())) {
      if (target.matches(glob.getTargetValues(link))) {
        result.add(glob);
      }
    }

    return result;
  }

  public Glob find(Key key) {
    if (key == null) {
      return null;
    }
    return globs.get(key.getGlobType(), key);
  }

  public Glob get(Key key) throws ItemNotFound {
    Glob glob = find(key);
    if (glob == null) {
      throw new ItemNotFound("Object " + key + " not found in repository");
    }
    return glob;
  }

  public Glob findUnique(GlobType type, FieldValue... values) {
    Glob result = null;
    for (Glob glob : globs.values(type)) {
      if (glob.matches(values)) {
        if (result != null) {
          StringBuilder builder = new StringBuilder();
          for (FieldValue value : values) {
            builder.append("(").append(value.getField()).append(",").append(value.getValue()).append(")");
          }
          throw new ItemAmbiguity("There are several objects of type " + type.getName() +
                                  " with values " + builder.toString());
        }
        result = glob;
      }
    }
    return result;
  }

  public GlobList getAll(GlobType type, GlobMatcher matcher) {
    if (GlobMatchers.NONE.equals(matcher)) {
      return new GlobList();
    }
    if (GlobMatchers.ALL.equals(matcher)) {
      return getAll(type);
    }
    GlobList result = new GlobList();
    for (Glob glob : globs.values(type)) {
      if (matcher.matches(glob, this)) {
        result.add(glob);
      }
    }
    return result;
  }

  public Glob findUnique(GlobType type, GlobMatcher matcher) throws ItemAmbiguity {
    Glob result = null;
    for (Glob glob : globs.values(type)) {
      if (matcher.matches(glob, this)) {
        if (result != null) {
          throw new ItemAmbiguity("There are several objects of type " + type.getName() +
                                  " matching: " + matcher);
        }
        result = glob;
      }
    }
    return result;
  }

  public Set<GlobType> getTypes() {
    return globs.keys();
  }

  public Integer getNextId(IntegerField field, int count) {
    return idGenerator.getNextId(field, count);
  }

  public Glob create(GlobType type, FieldValue... values) throws MissingInfo {
    Object[] globValuesArray = completeValues(type, values);
    addKeyValues(type, globValuesArray);
    Key key = KeyBuilder.createFromValues(type, globValuesArray);
    return create(type, key, globValuesArray);
  }

  public Glob create(Key key, FieldValue... values) throws ItemAlreadyExists {
    Object[] globValuesArray = completeValues(key.getGlobType(), values);
    for (Field field : key.getGlobType().getKeyFields()) {
      globValuesArray[field.getIndex()] = key.getValue(field);
    }
    return create(key.getGlobType(), key, globValuesArray);
  }

  private Glob create(GlobType type, Key key, Object[] globValuesArray) {
    checkKeyDoesNotExist(key);
    final DefaultGlob glob = new DefaultGlob(type, globValuesArray);
    globs.put(key.getGlobType(), key, glob);

    IndexTables indexTables = indexManager.getAssociatedTable(type);
    if (indexTables != null) {
      indexTables.add(glob);
    }

    changeSetToDispatch.processCreation(key, glob);
    notifyListeners();
    return glob;
  }

  private Object[] completeValues(GlobType type, FieldValue[] values) {
    Object[] completedArray = new Object[type.getFieldCount()];
    addDefaultValues(type, completedArray);
    copyValues(values, completedArray);
    return completedArray;
  }

  private void copyValues(FieldValue[] values, Object[] completedArray) {
    for (FieldValue value : values) {
      completedArray[value.getField().getIndex()] = value.getValue();
    }
  }

  private void copyValues(FieldValues values, final Object[] completedArray) {
    values.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        completedArray[field.getIndex()] = value;
      }
    });
  }

  private void addKeyValues(GlobType type, Object[] values) {
    List<Field> keyFields = type.getKeyFields();
    if (keyFields.size() != 1) {
      return;
    }
    Field field = keyFields.get(0);
    if ((values[field.getIndex()] != null) || !(field instanceof IntegerField)) {
      return;
    }
    IntegerField keyField = (IntegerField)field;
    values[keyField.getIndex()] = idGenerator.getNextId(keyField, 1);
  }

  private void addDefaultValues(GlobType type, Object[] values) {
    for (Field field : type.getFields()) {
      values[field.getIndex()] = field.getDefaultValue();
    }
  }

  public Glob findOrCreate(Key key, FieldValue... valuesForCreate) {
    GlobType type = key.getGlobType();
    Glob glob = globs.get(type, key);
    if (glob != null) {
      return glob;
    }

    Object[] completedArray = new Object[type.getFieldCount()];
    addDefaultValues(type, completedArray);
    copyValues(valuesForCreate, completedArray);
    copyValues(key, completedArray);
    return create(key.getGlobType(), key, completedArray);
  }

  public void update(Key key, Field field, Object newValue) throws ItemNotFound {
    MutableGlob mutableGlob = getGlobForUpdate(key);
    if (doUpdate(mutableGlob, key, field, newValue)) {
      notifyListeners();
    }
  }

  public void update(final Key key, FieldValue... values) throws ItemNotFound {
    final MutableGlob mutableGlob = getGlobForUpdate(key);
    enterBulkDispatchingMode();
    try {
      for (FieldValue value : values) {
        doUpdate(mutableGlob, key, value.getField(), value.getValue());
      }
    }
    finally {
      completeBulkDispatchingMode();
    }
  }

  private boolean doUpdate(MutableGlob mutableGlob, Key key, Field field, Object newValue) {
    GlobType globType = mutableGlob.getType();
    if (!field.getGlobType().equals(globType)) {
      throw new InvalidParameter("'" + field + "' is not a field of type '" +
                                 globType.getName() + "'");
    }
    Object oldValue = mutableGlob.getValue(field);
    if (Utils.equal(oldValue, newValue)) {
      return false;
    }
    if (globType.isKeyField(field)) {
      throw new OperationDenied("Field '" + field.getName() + "' of object '" +
                                key + "' is a key and cannot be changed");
    }

    mutableGlob.setObject(field, newValue);
    IndexTables indexTables = indexManager.getAssociatedTable(field);
    if (indexTables != null) {
      indexTables.add(newValue, mutableGlob, field, oldValue);
    }
    changeSetToDispatch.processUpdate(key, field, newValue);
    return true;
  }


  public GlobList findByIndex(Index index, Object value) {
    return indexManager.getAssociatedTable(index).findByIndex(value);
  }

  public MultiFieldIndexed findByIndex(MultiFieldIndex multiFieldIndex, Field field, Object value) {
    MultiFieldIndexed table = indexManager.getAssociatedTable(multiFieldIndex);
    if (table != null) {
      return table.findByIndex(field, value);
    }
    return null;
  }

  public void setTarget(final Key sourceKey, Link link, final Key targetKey) throws ItemNotFound {
    final MutableGlob sourceGlob = getGlobForUpdate(sourceKey);
    GlobType sourceType = sourceKey.getGlobType();
    if (!link.getSourceType().equals(sourceType)) {
      throw new InvalidParameter(
        "Type '" + sourceType.getName() + "' is not a valid source for link  '" + link + "'");
    }
    if (targetKey != null) {
      GlobType targetType = targetKey.getGlobType();
      if (!link.getTargetType().equals(targetType)) {
        throw new InvalidParameter("Key '" + targetKey + "' is not a valid target for link '" +
                                   link + "'");
      }
    }
    link.apply(new FieldMappingFunctor() {
      public void process(Field sourceField, Field targetField) {
        Object value = targetKey != null ? targetKey.getValue(targetField) : null;
        sourceGlob.setObject(sourceField, value);
        changeSetToDispatch.processUpdate(sourceKey, sourceField, value);
      }
    });
    notifyListeners();
  }

  public void delete(Key key) throws ItemNotFound, OperationDenied {
    MutableGlob glob = getGlobForUpdate(key);
    GlobType type = glob.getType();
    IndexTables indexTables = indexManager.getAssociatedTable(type);
    if (indexTables != null) {
      indexTables.remove(glob);
    }
    disable(glob);
    globs.remove(key.getGlobType(), key);
    changeSetToDispatch.processDeletion(key, glob);
    notifyListeners();
  }

  public void delete(GlobList list) throws OperationDenied {
    enterBulkDispatchingMode();
    OperationDenied exception = null;
    try {
      List<Pair<Key, FieldValues>> toBeRemoved = new ArrayList<Pair<Key, FieldValues>>();
      for (Glob glob : list) {
        if (!MutableGlob.class.isInstance(glob)) {
          throw new OperationDenied("Object '" + glob + "' cannot be modified");
        }
        GlobType type = glob.getType();
        IndexTables indexTables = indexManager.getAssociatedTable(type);
        if (indexTables != null) {
          indexTables.remove(glob);
        }
        disable(glob);
        toBeRemoved.add(new Pair<Key, FieldValues>(glob.getKey(), glob));
      }
      for (Pair<Key, FieldValues> pair : toBeRemoved) {
        Key key = pair.getFirst();
        globs.remove(key.getGlobType(), key);
        changeSetToDispatch.processDeletion(key, pair.getSecond());
      }
    }
    catch (OperationDenied e) {
      exception = e;
    }
    finally {
      completeBulkDispatchingMode();
    }
    if (exception != null) {
      throw exception;
    }
  }

  public void deleteAll(GlobType... types) throws OperationDenied {
    enterBulkDispatchingMode();
    OperationDenied exception = null;
    try {
      List<Pair<Key, FieldValues>> toBeRemoved = new ArrayList<Pair<Key, FieldValues>>();
      for (GlobType type : types) {
        for (Map.Entry<Key, Glob> entry : globs.get(type).entrySet()) {
          if (!MutableGlob.class.isInstance(entry.getValue())) {
            throw new OperationDenied("Object '" + entry.getKey() + "' cannot be modified");
          }
          Glob glob = entry.getValue();
          disable(glob);
          toBeRemoved
            .add(new Pair<Key, FieldValues>(entry.getKey(), entry.getValue()));
        }
        IndexTables indexTables = indexManager.getAssociatedTable(type);
        if (indexTables != null) {
          indexTables.removeAll();
        }
      }
      for (Pair<Key, FieldValues> pair : toBeRemoved) {
        Key key = pair.getFirst();
        globs.remove(key.getGlobType(), key);
        changeSetToDispatch.processDeletion(key, pair.getSecond());
      }
    }
    catch (OperationDenied e) {
      exception = e;
    }
    finally {
      completeBulkDispatchingMode();
    }
    if (exception != null) {
      throw exception;
    }
  }

  public void apply(ChangeSet changeSet) throws InvalidParameter {
    changeSet.safeVisit(new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) {
        if (find(key) != null) {
          throw new InvalidParameter("Object " + key + " already exists");
        }
      }

      public void visitUpdate(Key key, FieldValues values) {
        if (find(key) == null) {
          throw new InvalidParameter("Object " + key + " not found - cannot apply update");
        }
      }

      public void visitDeletion(Key key, FieldValues values) {
        if (find(key) == null) {
          throw new InvalidParameter("Object " + key + " not found - cannot apply deletion");
        }
      }
    });

    try {
      enterBulkDispatchingMode();
      changeSet.safeVisit(new ChangeSetExecutor());
    }
    finally {
      completeBulkDispatchingMode();
    }
  }

  private void disable(Glob glob) {
    if (glob instanceof AbstractGlob) {
      ((AbstractGlob)glob).dispose();
    }
  }

  public void enterBulkDispatchingMode() {
    bulkDispatchingModeLevel++;
  }

  public void completeBulkDispatchingMode() {
    if (bulkDispatchingModeLevel < 0) {
      return;
    }
    bulkDispatchingModeLevel--;
    if (bulkDispatchingModeLevel == 0) {
      notifyListeners();
    }
  }

  public void reset(GlobList newGlobs, GlobType... changedTypes) {
    enterBulkDispatchingMode();
    if (changedTypes.length == 0) {
      Set<GlobType> allTypes = getTypes();
      changedTypes = allTypes.toArray(new GlobType[allTypes.size()]);
    }
    List typesList = Arrays.asList(changedTypes);
    for (GlobType type : changedTypes) {
      for (Map.Entry<Key, Glob> entry : globs.get(type).entrySet()) {
        disable(entry.getValue());
      }
      globs.removeAll(type);
      add(type.getConstants());
    }
    for (Glob glob : newGlobs) {
      Key key = glob.getKey();
      if (typesList.contains(key.getGlobType())) {
        globs.put(key.getGlobType(), key, glob.duplicate());
      }
    }
    for (ChangeSetListener listener : triggers) {
      listener.globsReset(this, typesList);
    }
    for (ChangeSetListener listener : changeListeners) {
      listener.globsReset(this, typesList);
    }
    completeBulkDispatchingMode();
  }

  public GlobIdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void add(GlobList globs) {
    for (Glob glob : globs) {
      Key key = glob.getKey();
      checkKeyDoesNotExist(key);
      this.globs.put(key.getGlobType(), key, glob);
    }
  }

  public void add(Glob... globs) {
    add(new GlobList(globs));
  }

  public void addChangeListener(ChangeSetListener listener) {
    changeListeners = new ArrayList<ChangeSetListener>(changeListeners);
    changeListeners.add(listener);
  }

  public void removeChangeListener(ChangeSetListener listener) {
    changeListeners = new ArrayList<ChangeSetListener>(changeListeners);
    changeListeners.remove(listener);
  }

  private void notifyListeners() {
    if (bulkDispatchingModeLevel > 0) {
      return;
    }
    if (this.changeSetToDispatch.isEmpty()) {
      return;
    }
    bulkDispatchingModeLevel++;
    MutableChangeSet currentChangeSetToDispatch = this.changeSetToDispatch;
    try {
      this.changeSetToDispatch = new DefaultChangeSet();
      for (ChangeSetListener trigger : triggers) {
        trigger.globsChanged(currentChangeSetToDispatch, this);
      }
      currentChangeSetToDispatch.merge(this.changeSetToDispatch);
    }
    finally {
      bulkDispatchingModeLevel--;
    }
    this.changeSetToDispatch = new DefaultChangeSet();
    for (ChangeSetListener listener : changeListeners) {
      listener.globsChanged(currentChangeSetToDispatch, this);
    }
  }

  public void addTrigger(ChangeSetListener trigger) {
    triggers.add(trigger);
  }

  public void removeTrigger(ChangeSetListener trigger) {
    triggers.remove(trigger);
  }

  private MutableGlob getGlobForUpdate(Key key) throws ItemNotFound, OperationDenied {
    Glob glob = globs.get(key.getGlobType(), key);
    if (glob == null) {
      throw new ItemNotFound("Object '" + key + "' does not exist");
    }

    // optimization (class.isInstance is expensive)
    if (!glob.getClass().equals(DefaultGlob.class) && !MutableGlob.class.isInstance(glob)) {
      throw new OperationDenied("Object '" + key + "' cannot be modified");
    }
    return (MutableGlob)glob;
  }

  private void checkKeyDoesNotExist(Key key) throws ItemAlreadyExists {
    if (globs.containsKey(key.getGlobType(), key)) {
      throw new ItemAlreadyExists("An object with key " + key + " already exists");
    }
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    GlobList globs = getAll();
    Collections.sort(globs, new Comparator<Glob>() {
      public int compare(Glob glob1, Glob glob2) {
        return glob1.getKey().toString().compareTo(glob2.getKey().toString());
      }
    });
    XmlGlobWriter.write(globs, this, writer);
    return writer.toString();
  }

  private class ChangeSetExecutor implements ChangeSetVisitor {
    public void visitCreation(Key key, FieldValues values) {
      FieldValues mergedValues = FieldValuesBuilder.init().set(key).set(values).get();
      create(key.getGlobType(), mergedValues.toArray());
    }

    public void visitUpdate(Key key, FieldValues values) {
      update(key, values.toArray());
    }

    public void visitDeletion(Key key, FieldValues values) {
      delete(key);
    }
  }

  public void checkConsistency() {
    boolean firstCall = true;
    for (Iterator<Glob> iterator = globs.iterator(); iterator.hasNext();) {
      final Glob glob = iterator.next();
      Link[] links = glob.getType().getOutboundLinks();
      for (Link link : links) {
        final FieldValuesBuilder valuesBuilder = FieldValuesBuilder.init();
        link.apply(new FieldMappingFunctor() {
          public void process(Field sourceField, Field targetField) {
            Object value = glob.getValue(sourceField);
            if (value != null) {
              valuesBuilder.setObject(targetField, value);
            }
          }
        });
        FieldValues targetValues = valuesBuilder.get();
        if (targetValues.size() == link.getTargetType().getKeyFields().size()) {
          Key key = KeyBuilder.createFromValues(link.getTargetType(), targetValues);
          Glob target = find(key);
          if (target == null) {
            if (firstCall) {
              new Exception().printStackTrace();
              firstCall = false;
            }
            System.out.println(glob.getKey() + " ==> " + key);
          }
        }
      }
    }
  }
}
