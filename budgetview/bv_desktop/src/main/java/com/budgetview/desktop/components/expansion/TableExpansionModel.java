package com.budgetview.desktop.components.expansion;

import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class TableExpansionModel implements GlobMatcher, ChangeSetListener {
  private Map<Key, NodeState> nodeStatesMap = new HashMap<Key, NodeState>();
  private GlobRepository repository;
  private Directory directory;
  private ExpandableTable table;
  private GlobType type;
  private GlobMatcher baseMatcher = GlobMatchers.ALL;

  public TableExpansionModel(GlobType type, GlobRepository repository, Directory directory, ExpandableTable table) {
    this.repository = repository;
    this.directory = directory;
    this.table = table;
    repository.addChangeListener(this);
    this.type = type;
    updateNodeStates();
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

  protected abstract boolean hasChildren(Key key, GlobMatcher baseMatcher, GlobRepository repository);

  public abstract boolean isRoot(Glob glob);

  public abstract boolean isParent(Glob glob);

  protected abstract Key getParentKey(Glob glob);

  protected abstract boolean isExpansionAuthorized(Glob glob);

  private void updateNodeStates() {
    nodeStatesMap.clear();
    for (Glob master : repository.getAll(type, getMasterMatcher())) {
      Key key = master.getKey();
      setState(key,
               isExpansionAuthorized(master) && hasChildren(key, baseMatcher, repository),
               true);
    }
  }

  public void toggleExpansion(Glob glob, boolean selectIfNeeded) {
    if (!isExpandable(glob)) {
      return;
    }
    Key key = glob.getKey();

    if (selectIfNeeded) {
      SelectionService selectionService = directory.get(SelectionService.class);
      GlobList selection = selectionService.getSelection(glob.getType());
      if (!selection.contains(glob)) {
        selectionService.select(glob);
      }
    }

    NodeState state = getState(key);
    state.expanded = !state.expanded;
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
    return getState(glob.getKey()).expandable;
  }

  public boolean matches(Glob glob, GlobRepository repository) {
    if (!baseMatcher.matches(glob, repository)) {
      return false;
    }
    if (isRoot(glob)) {
      return true;
    }
    Key parentKey = getParentKey(glob);
    Glob parent = repository.get(parentKey);
    return matches(parent, repository) && getState(parentKey).expanded;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsCreationsOrDeletions(type)) {
      return;
    }
    updateNodeStates();
    Set<Key> createdList = changeSet.getCreated(type);
    for (Key key : createdList) {
      Glob created = repository.get(key);
      Key master = getParentKey(created);
      if (master != null) {
        setState(master, isExpansionAuthorized(created), true);
      }
    }
    Set<Key> deletedList = changeSet.getDeleted(type);
    for (Key key : deletedList) {
      nodeStatesMap.remove(key);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      updateNodeStates();
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
