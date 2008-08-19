package org.globsframework.model.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.index.Index;
import org.globsframework.metamodel.index.MultiFieldIndex;
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.*;

import java.util.Set;

public class GlobRepositoryDecorator implements GlobRepository {
  protected GlobRepository repository;

  public GlobRepositoryDecorator(GlobRepository repository) {
    this.setRepository(repository);
  }

  public Glob find(Key key) {
    return getRepository().find(key);
  }

  public Glob get(Key key) throws ItemNotFound {
    return getRepository().get(key);
  }

  public Glob findUnique(GlobType type, FieldValue... values) throws ItemAmbiguity {
    return getRepository().findUnique(type, values);
  }

  public GlobList getAll(GlobType... type) {
    return getRepository().getAll(type);
  }

  public GlobList getAll(GlobType type, GlobMatcher matcher) {
    return getRepository().getAll(type, matcher);
  }

  public Glob findUnique(GlobType type, GlobMatcher matcher) throws ItemAmbiguity {
    return getRepository().findUnique(type, matcher);
  }

  public GlobList findByIndex(Index index, Object value) {
    return getRepository().findByIndex(index, value);
  }

  public MultiFieldIndexed findByIndex(MultiFieldIndex multiFieldIndex, Field field, Object value) {
    return getRepository().findByIndex(multiFieldIndex, field, value);
  }

  public Set<GlobType> getTypes() {
    return getRepository().getTypes();
  }

  public Glob findLinkTarget(Glob source, Link link) {
    return getRepository().findLinkTarget(source, link);
  }

  public GlobList findLinkedTo(Glob target, Link link) {
    return getRepository().findLinkedTo(target, link);
  }

  public Integer getNextId(IntegerField field, int count) {
    return getRepository().getNextId(field, count);
  }

  public Glob create(GlobType type, FieldValue... values) throws MissingInfo, ItemAlreadyExists {
    return getRepository().create(type, values);
  }

  public Glob create(Key key, FieldValue... values) throws ItemAlreadyExists {
    return getRepository().create(key, values);
  }

  public Glob findOrCreate(Key key, FieldValue... defaultValues) throws MissingInfo {
    return getRepository().findOrCreate(key, defaultValues);
  }

  public void update(Key key, Field field, Object newValue) throws ItemNotFound {
    getRepository().update(key, field, newValue);
  }

  public void update(Key key, FieldValue... values) {
    getRepository().update(key, values);
  }

  public void setTarget(Key source, Link link, Key target) throws ItemNotFound {
    getRepository().setTarget(source, link, target);
  }

  public void delete(Key key) throws ItemNotFound, OperationDenied {
    getRepository().delete(key);
  }

  public void delete(GlobList list) throws OperationDenied {
    getRepository().delete(list);
  }

  public void deleteAll(GlobType... types) throws OperationDenied {
    getRepository().deleteAll(types);
  }

  public void apply(ChangeSet changeSet) throws InvalidParameter {
    getRepository().apply(changeSet);
  }

  public void addTrigger(ChangeSetListener listener) {
    getRepository().addTrigger(listener);
  }

  public void removeTrigger(ChangeSetListener listener) {
    getRepository().removeTrigger(listener);
  }

  public void addChangeListener(ChangeSetListener listener) {
    getRepository().addChangeListener(listener);
  }

  public void removeChangeListener(ChangeSetListener listener) {
    getRepository().removeChangeListener(listener);
  }

  public void enterBulkDispatchingMode() {
    getRepository().enterBulkDispatchingMode();
  }

  public void completeBulkDispatchingMode() {
    getRepository().completeBulkDispatchingMode();
  }

  public void completeBulkDispatchingModeWithoutTriggers() {
    getRepository().completeBulkDispatchingModeWithoutTriggers();
  }

  public void reset(GlobList newGlobs, GlobType... changedTypes) {
    getRepository().reset(newGlobs, changedTypes);
  }

  protected GlobRepository getRepository() {
    return repository;
  }

  protected void setRepository(GlobRepository repository) {
    this.repository = repository;
  }

  public GlobIdGenerator getIdGenerator() {
    return repository.getIdGenerator();
  }
}
