package org.designup.picsou.gui.components.expansion;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class TableExpansionModel implements GlobMatcher, ChangeSetListener {
  private Map<Integer, Boolean> expandedMap = new HashMap<Integer, Boolean>();
  private Map<Integer, Boolean> expandableMap = new HashMap<Integer, Boolean>();
  private GlobRepository repository;
  private ExpandableTable table;
  private IntegerField idField;
  private GlobType type;
  private GlobMatcher baseMatcher = GlobMatchers.ALL;

  public TableExpansionModel(GlobType type, IntegerField idField, GlobRepository repository, ExpandableTable table, final boolean isInitiallyExpanded) {
    this.idField = idField;
    this.repository = repository;
    this.table = table;
    repository.addChangeListener(this);
    this.type = type;
    for (Glob master : repository.getAll(type, getMasterMatcher())) {
      expandedMap.put(master.get(idField), isInitiallyExpanded);
    }
    updateExpandabilities(false);
  }

  public void setBaseMatcher(GlobMatcher baseMatcher) {
    this.baseMatcher = baseMatcher;
  }

  public void completeInit() {
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(type)) {
          table.setFilter(TableExpansionModel.this);
        }
      }
    });
  }

  protected abstract GlobMatcher getMasterMatcher();

  protected abstract boolean hasChildren(Integer id, GlobRepository repository);

  public abstract boolean isMaster(Glob glob);

  protected abstract Integer getMasterId(Glob glob);

  public abstract boolean isExpansionDisabled(Glob glob);

  private void updateExpandabilities(boolean forceUpdateExpanded) {
    expandableMap.clear();
    if (forceUpdateExpanded){
      expandedMap.clear();
    }
    for (Glob master : repository.getAll(type, getMasterMatcher())) {
      Integer id = master.get(idField);
      boolean expandable = hasChildren(id, repository);
      expandableMap.put(id, expandable);
      if (!expandable || forceUpdateExpanded) {
        expandedMap.put(id, expandable);
      }
    }
  }

  public void toggleExpansion(Glob glob) {
    if (!isExpandable(glob)) {
      return;
    }
    Integer id = glob.get(idField);
    Boolean existingValue = expandedMap.get(id);
    expandedMap.put(id, !existingValue);
    table.setFilter(this);
  }

  public void expandAll() {
    setExpanded(true);
  }

  public void collapseAll() {
    setExpanded(false);
  }

  private void setExpanded(boolean expanded) {
    for (Map.Entry<Integer, Boolean> entry : expandedMap.entrySet()) {
      Boolean expandable = expandableMap.get(entry.getKey());
      entry.setValue(expanded && (expandable != null && expandable));
    }
    table.setFilter(this);
  }

  public boolean isExpanded(Glob glob) {
    if (!isMaster(glob)) {
      return false;
    }
    Integer id = glob.get(idField);
    return expandedMap.get(id);
  }

  public boolean isExpandable(Glob glob) {
    if (glob == null) {
      return false;
    }
    Boolean status = expandableMap.get(glob.get(idField));
    return Boolean.TRUE.equals(status);
  }

  public boolean matches(Glob glob, GlobRepository repository) {
    if (!baseMatcher.matches(glob, repository)) {
      return false;
    }
    if (isMaster(glob)) {
      return true;
    }
    Integer masterId = getMasterId(glob);
    if (masterId == null) {
      return true;
    }
    return expandedMap.get(masterId);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsCreationsOrDeletions(type)) {
      return;
    }
    updateExpandabilities(false);
    Set<Key> createdList = changeSet.getCreated(type);
    for (Key key : createdList) {
      Glob created = repository.get(key);
      Integer master = getMasterId(created);
      if (master != null) {
        expandedMap.put(master, true);
      }
    }
    Set<Key> deletedList = changeSet.getDeleted(type);
    for (Key key : deletedList) {
      expandedMap.remove(key.get(idField));
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      updateExpandabilities(true);
    }
  }
}
