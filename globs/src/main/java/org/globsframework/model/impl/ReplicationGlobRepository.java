package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.index.Index;
import org.globsframework.metamodel.index.MultiFieldIndex;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobRepositoryDecorator;
import org.globsframework.remote.SerializedRemoteAccess;
import org.globsframework.utils.exceptions.*;

import java.util.*;

public class ReplicationGlobRepository extends GlobRepositoryDecorator implements GlobRepository {
  private DefaultGlobRepository localRepository = new DefaultGlobRepository();
  private Set<GlobType> types = new HashSet<GlobType>();

  public ReplicationGlobRepository(GlobRepository repository, GlobType... types) {
    super(repository);
    this.types.addAll(Arrays.asList(types));
  }

  public Glob find(Key key) {
    if (types.contains(key.getGlobType())) {
      return localRepository.find(key);
    }
    else {
      return super.find(key);
    }
  }

  public Glob get(Key key) throws ItemNotFound {
    if (types.contains(key.getGlobType())) {
      return localRepository.get(key);
    }
    else {
      return super.get(key);
    }
  }

  public Glob findUnique(GlobType type, FieldValue... values) throws ItemAmbiguity {
    if (types.contains(type)) {
      return localRepository.findUnique(type, values);
    }
    else {
      return super.findUnique(type, values);
    }
  }

  public GlobList getAll(GlobType... type) {
    GlobList globs = localRepository.getAll(type);
    globs.addAll(super.getAll(type));
    return globs;
  }

  public GlobList getAll(GlobType type, final GlobMatcher matcher) {
    GlobMatcher globMatcher = new DecoratedGlobMatcher(matcher);
    if (types.contains(type)) {
      return localRepository.getAll(type, globMatcher);
    }
    else {
      return super.getAll(type, globMatcher);
    }
  }

  public void apply(GlobType type, GlobMatcher matcher, GlobFunctor callback) throws Exception {
    GlobMatcher globMatcher = new DecoratedGlobMatcher(matcher);
    if (types.contains(type)) {
      localRepository.apply(type, globMatcher, callback);
    }
    else {
      super.apply(type, globMatcher, callback);
    }
  }

  public void safeApply(GlobType type, GlobMatcher matcher, GlobFunctor callback) {
    GlobMatcher globMatcher = new DecoratedGlobMatcher(matcher);
    if (types.contains(type)) {
      localRepository.safeApply(type, globMatcher, callback);
    }
    else {
      super.safeApply(type, globMatcher, callback);
    }
  }

  public Glob findUnique(GlobType type, GlobMatcher matcher) throws ItemAmbiguity {
    GlobMatcher globMatcher = new DecoratedGlobMatcher(matcher);
    if (types.contains(type)) {
      return localRepository.findUnique(type, globMatcher);
    }
    else {
      return super.findUnique(type, globMatcher);
    }
  }

  public SortedSet<Glob> getSorted(GlobType globType, Comparator<Glob> comparator, GlobMatcher matcher) {
    GlobMatcher globMatcher = new DecoratedGlobMatcher(matcher);

    if (types.contains(globType)) {
      return localRepository.getSorted(globType, comparator, globMatcher);
    }
    else {
      return super.getSorted(globType, comparator, globMatcher);
    }
  }

  public GlobList findByIndex(Index index, Object value) {
    if (types.contains(index.getField().getGlobType())) {
      return localRepository.findByIndex(index, value);
    }
    else {
      return super.findByIndex(index, value);
    }
  }

  public MultiFieldIndexed findByIndex(MultiFieldIndex multiFieldIndex, Field field, Object value) {
    if (types.contains(field.getGlobType())) {
      return localRepository.findByIndex(multiFieldIndex, field, value);
    }
    else {
      return super.findByIndex(multiFieldIndex, field, value);
    }
  }

  public Set<GlobType> getTypes() {
    HashSet<GlobType> types = new HashSet<GlobType>();
    types.addAll(this.types);
    types.addAll(super.getTypes());
    return types;
  }

  public Glob findLinkTarget(Glob source, Link link) {
    if (types.contains(link.getTargetType())) {
      return localRepository.findLinkTarget(source, link);
    }
    else {
      return super.findLinkTarget(source, link);
    }
  }

  public GlobList findLinkedTo(Glob target, Link link) {
    if (types.contains(link.getSourceType())) {
      return localRepository.findLinkedTo(target, link);
    }
    else {
      return super.findLinkedTo(target, link);
    }
  }

  public Integer getNextId(IntegerField field, int count) {
    if (types.contains(field.getGlobType())) {
      return localRepository.getNextId(field, count);
    }
    else {
      return super.getNextId(field, count);
    }
  }

  public Glob create(GlobType type, FieldValue... values) throws MissingInfo, ItemAlreadyExists {
    if (types.contains(type)) {
      return localRepository.create(type, values);
    }
    else {
      return super.create(type, values);
    }
  }

  public Glob create(Key key, FieldValue... values) throws ItemAlreadyExists {
    if (types.contains(key.getGlobType())) {
      return localRepository.create(key, values);
    }
    else {
      return super.create(key, values);
    }
  }

  public Glob findOrCreate(Key key, FieldValue... defaultValues) throws MissingInfo {
    if (types.contains(key.getGlobType())) {
      return localRepository.findOrCreate(key, defaultValues);
    }
    else {
      return super.findOrCreate(key, defaultValues);
    }
  }

  public boolean contains(GlobType type) {
    return localRepository.contains(type) || super.contains(type);
  }

  public boolean contains(GlobType type, GlobMatcher matcher) {
    GlobMatcher globMatcher = new DecoratedGlobMatcher(matcher);

    return localRepository.contains(type, globMatcher) || super.contains(type, globMatcher);
  }

  public void update(Key key, Field field, Object newValue) throws ItemNotFound {
    if (types.contains(key.getGlobType())) {
      localRepository.update(key, field, newValue);
    }
    else {
      super.update(key, field, newValue);
    }
  }

  public void update(Key key, FieldValue... values) {
    if (types.contains(key.getGlobType())) {
      localRepository.update(key, values);
    }
    else {
      super.update(key, values);
    }
  }

  public void setTarget(Key source, Link link, Key target) throws ItemNotFound {
    if (types.contains(source.getGlobType())) {
      localRepository.setTarget(source, link, target);
    }
    else {
      super.setTarget(source, link, target);
    }
  }

  public void delete(Key key) throws ItemNotFound, OperationDenied {
    if (types.contains(key.getGlobType())) {
      localRepository.delete(key);
    }
    else {
      super.delete(key);
    }
  }

  public void delete(GlobList list) throws OperationDenied {
    GlobList localDelete = list.filter(new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return types.contains(item.getType());
      }
    }, localRepository);
    localRepository.delete(localDelete);
    GlobList remoteDelete = list.filter(new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return !types.contains(item.getType());
      }
    }, localRepository);

    super.delete(remoteDelete);
  }

  public void deleteAll(GlobType... types) throws OperationDenied {
    for (GlobType type : types) {
      if (this.types.contains(type)) {
        localRepository.deleteAll(type);
      }
      else {
        super.deleteAll(types);
      }
    }
  }

  public void apply(ChangeSet changeSet) throws InvalidParameter {
    final DefaultChangeSet localChangeSet = new DefaultChangeSet();
    final DefaultChangeSet remoteChangeSet = new DefaultChangeSet();
    changeSet.safeVisit(new SerializedRemoteAccess.ChangeVisitor() {
      public void complete() {
      }

      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (types.contains(key.getGlobType())) {
          localChangeSet.processCreation(key, values);
        }
        else {
          remoteChangeSet.processCreation(key, values);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (types.contains(key.getGlobType())) {
          localChangeSet.processUpdate(key, values);
        }
        else {
          remoteChangeSet.processUpdate(key, values);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        if (types.contains(key.getGlobType())) {
          localChangeSet.processDeletion(key, previousValues);
        }
        else {
          remoteChangeSet.processDeletion(key, previousValues);
        }
      }
    });
    localRepository.apply(localChangeSet);
    super.apply(remoteChangeSet);
  }

  public void addTrigger(ChangeSetListener listener) {
    localRepository.addTrigger(listener);
    super.addTrigger(listener);
  }

  public void removeTrigger(ChangeSetListener listener) {
    localRepository.removeTrigger(listener);
    super.removeTrigger(listener);
  }

  public void addChangeListener(ChangeSetListener listener) {
    localRepository.addChangeListener(listener);
    super.addChangeListener(listener);
  }

  public void removeChangeListener(ChangeSetListener listener) {
    localRepository.removeChangeListener(listener);
    super.removeChangeListener(listener);
  }

  public void startChangeSet() {
    localRepository.startChangeSet();
  }

  public void completeChangeSet() {
    localRepository.completeChangeSet();
  }

  public void completeChangeSetWithoutTriggers() {
    localRepository.completeChangeSetWithoutTriggers();
  }

  public void reset(GlobList newGlobs, GlobType... changedTypes) {
    GlobList localList = new GlobList();
    GlobList remoteList = new GlobList();
    for (Glob glob : newGlobs) {
      if (types.contains(glob.getType())) {
        localList.add(glob);
      }
      else {
        remoteList.add(glob);
      }
    }
    List<GlobType> localTypes = new ArrayList<GlobType>();
    List<GlobType> remoteTypes = new ArrayList<GlobType>();
    for (GlobType type : changedTypes) {
      if (types.contains(type)) {
        localTypes.add(type);
      }
      else {
        remoteTypes.add(type);
      }
    }
    localRepository.reset(localList, localTypes.toArray(new GlobType[localTypes.size()]));
    super.reset(remoteList, remoteTypes.toArray(new GlobType[remoteTypes.size()]));
  }

  public GlobIdGenerator getIdGenerator() {
    return super.getIdGenerator();
  }

  private class DecoratedGlobMatcher implements GlobMatcher {
    private final GlobMatcher matcher;

    public DecoratedGlobMatcher(GlobMatcher matcher) {
      this.matcher = matcher;
    }

    public boolean matches(Glob item, GlobRepository repository) {
      return matcher.matches(item, ReplicationGlobRepository.this);
    }
  }
}
