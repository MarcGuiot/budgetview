package org.globsframework.gui.views;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobKeyMatcher;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.SortedGlobList;

import java.util.*;

public class GlobViewModel implements ChangeSetListener {
  private SortedGlobList globs;
  private GlobType type;
  private GlobRepository repository;
  private Listener listener;
  private GlobMatcher matcher = GlobMatchers.ALL;
  private boolean showNullElement = false;

  public interface Listener {
    void globInserted(int index);

    void globUpdated(int index);

    void globRemoved(int index);

    void globListPreReset();

    void globListReset();
  }

  public GlobViewModel(GlobType type, GlobRepository repository, Comparator<Glob> comparator, Listener listener) {
    this(type, repository, comparator, false, listener);
  }

  public GlobViewModel(GlobType type, GlobRepository repository, Comparator<Glob> comparator, boolean showNullElement, Listener listener) {
    this.type = type;
    this.repository = repository;
    this.listener = listener;
    this.globs = new SortedGlobList(comparator);
    this.showNullElement = showNullElement;
    initList(false);
    repository.addChangeListener(this);
  }

  private void initList(boolean notify) {
    if (notify) {
      listener.globListPreReset();
    }
    globs.clear();
    if (showNullElement) {
      globs.add(null);
    }
    for (Glob glob : repository.getAll(type, matcher)) {
      globs.add(glob);
    }
    if (notify) {
      listener.globListReset();
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  public int indexOf(final Glob glob) {
    if (glob == null) {
      return globs.indexOf(null);
    }
    if (!glob.getType().equals(type)) {
      return -1;
    }
    return globs.firstIndexOf(new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return glob == item;
      }
    }, repository);
  }

  public GlobList getAll() {
    return globs.asList();
  }

  public Glob get(int index) {
    return globs.get(index);
  }

  public int size() {
    return globs.size();
  }

  public void setFilter(GlobMatcher matcher) {
    this.matcher = matcher;
    initList(true);
  }

  public void sort(Comparator<Glob> comparator) {
    listener.globListPreReset();
    globs.setComparator(comparator);
    listener.globListReset();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    int changeCount = changeSet.getChangeCount(type);
    if (changeCount == 0) {
      return;
    }
    try {
      if (changeCount == 1) {
        changeSet.safeVisit(type, new MinorChangesVisitor(repository));
      }
      else if (changeCount > 1) {
        MajorChangesVisitor visitor = new MajorChangesVisitor(repository);
        changeSet.safeVisit(type, visitor);
        visitor.complete();
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      initList(true);
    }
  }

  public void refresh() {
    for (Iterator<Glob> iter = globs.iterator(); iter.hasNext();) {
      Glob glob = iter.next();
      if (!glob.exists()) {
        iter.remove();
      }
    }
    globs.updateSorting();
  }

  public void clear() {
    globs.clear();
  }

  private class MinorChangesVisitor implements ChangeSetVisitor {
    private final GlobRepository repository;

    public MinorChangesVisitor(GlobRepository repository) {
      this.repository = repository;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      Glob glob = repository.get(key);
      if (matcher.matches(glob, repository)) {
        int index = globs.add(glob);
        listener.globInserted(index);
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      Glob glob = repository.get(key);
      boolean matches = matcher.matches(glob, repository);
      int previousIndex = globs.firstIndexOf(new GlobKeyMatcher(key), repository);
      if (previousIndex >= 0) {
        globs.remove(previousIndex);
        if (matches) {
          int newIndex = globs.add(repository.get(key));
          if (newIndex == previousIndex) {
            listener.globUpdated(newIndex);
          }
          else {
            listener.globRemoved(previousIndex);
            listener.globInserted(newIndex);
          }
        }
        else {
          listener.globRemoved(previousIndex);
        }
      }
      else if (matches) {
        int newIndex = globs.add(glob);
        listener.globInserted(newIndex);
      }
    }

    public void visitDeletion(Key key, FieldValues values) throws Exception {
      int index = globs.firstIndexOf(new GlobKeyMatcher(key), repository);
      if (index >= 0) {
        globs.remove(index);
        listener.globRemoved(index);
      }
    }
  }

  private class MajorChangesVisitor implements ChangeSetVisitor {
    private final GlobRepository repository;
    private GlobList toAdd = new GlobList();
    private GlobList toRemove = new GlobList();

    public MajorChangesVisitor(GlobRepository repository) {
      this.repository = repository;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      Glob glob = repository.find(key);
      if ((glob != null) && matcher.matches(glob, repository)) {
        toAdd.add(glob);
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      toRemove.add(globs.getFirst(new GlobKeyMatcher(key), repository));
      Glob glob = repository.get(key);
      toAdd.add(glob);
    }

    public void visitDeletion(Key key, FieldValues values) throws Exception {
      toRemove.add(globs.getFirst(new GlobKeyMatcher(key), repository));
    }

    public void complete() {
      if (toAdd.isEmpty() && toRemove.isEmpty()) {
        return;
      }
      Set newList = new HashSet(globs.asList());
      newList.removeAll(toRemove);

      for (Glob glob : toAdd) {
        if (matcher.matches(glob, repository)) {
          newList.add(glob);
        }
      }
      listener.globListPreReset();
      globs.clear();
      globs.addAll(newList);
      listener.globListReset();
    }
  }

  public String toString() {
    return "GlobModel(" + type.getName() + "): " + globs;
  }
}

