package org.designup.picsou.gui.components.expansion;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class TableExpansionModel implements GlobMatcher, ChangeSetListener {
  private Map<Key, NodeState> nodeStatesMap = new HashMap<Key, NodeState>();
  private GlobRepository repository;
  private ExpandableTable table;
  private GlobType type;
  private GlobMatcher baseMatcher = GlobMatchers.ALL;

  public TableExpansionModel(GlobType type, GlobRepository repository, ExpandableTable table, final boolean isInitiallyExpanded) {
    this.repository = repository;
    this.table = table;
    repository.addChangeListener(this);
    this.type = type;
    for (Glob master : repository.getAll(type, getMasterMatcher())) {
      setState(master.getKey(), true, isInitiallyExpanded);
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

  private GlobMatcher getMasterMatcher() {
    return new GlobMatcher() {
      public boolean matches(Glob wrapper, GlobRepository repository) {
        return isParent(wrapper);
      }
    };
  }

  protected abstract boolean hasChildren(Key key, GlobRepository repository);

  public abstract boolean isRoot(Glob glob);

  public abstract boolean isParent(Glob glob);

  protected abstract Key getParentKey(Glob glob);

  public abstract boolean isExpansionDisabled(Glob glob);

  private void updateExpandabilities(boolean forceUpdateExpanded) {
    nodeStatesMap.clear();
    for (Glob master : repository.getAll(type, getMasterMatcher())) {
      Key key = master.getKey();
      boolean expandable = hasChildren(key, repository);
      setState(key, expandable, true);
    }
  }

  public void toggleExpansion(Glob glob) {
    if (!isExpandable(glob)) {
      return;
    }
    Key key = glob.getKey();
    NodeState state = getState(key);
    setState(key, state.expandable, !state.expanded);
    table.setFilter(this);
  }

  public void expandAll() {
    setExpanded(true);
  }

  public void collapseAll() {
    setExpanded(false);
  }

  private void setExpanded(boolean expanded) {
    for (NodeState state : nodeStatesMap.values()) {
      state.expanded = expanded;
    }
    table.setFilter(this);
  }

  public boolean isExpanded(Glob glob) {
    if (!isParent(glob)) {
      return false;
    }
    return getState(glob.getKey()).expanded;
  }

  public boolean isExpandable(Glob glob) {
    if (glob == null) {
      return false;
    }
    return getState(glob.getKey()).expandable;
  }

  public boolean matches(Glob glob, GlobRepository repository) {
    if (!baseMatcher.matches(glob, repository)) {
      return false;
    }
    return isRoot(glob) || getState(getParentKey(glob)).expanded;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsCreationsOrDeletions(type)) {
      return;
    }
    updateExpandabilities(false);
    Set<Key> createdList = changeSet.getCreated(type);
    for (Key key : createdList) {
      Glob created = repository.get(key);
      Key master = getParentKey(created);
      if (master != null) {
        getState(master).expanded = true;
      }
    }
    Set<Key> deletedList = changeSet.getDeleted(type);
    for (Key key : deletedList) {
      nodeStatesMap.remove(key);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      updateExpandabilities(true);
    }
  }

  private void setState(Key key, boolean expandable, boolean expanded) {
    getState(key).set(expandable, expanded);
  }

  private NodeState getState(Key key) {
    NodeState nodeState = nodeStatesMap.get(key);
    if (nodeState == null) {
      nodeState = new NodeState();
      nodeStatesMap.put(key, nodeState);
    }
    return nodeState;
  }

  private static class NodeState {
    boolean expandable;
    boolean expanded;

    public void set(boolean expandable, boolean expanded) {
      this.expandable = expandable;
      this.expanded = expanded;
    }
  }
}
