package org.globsframework.gui.views;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

    void globMoved(int previousIndex, int newIndex);

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
    reloadGlobList();
    if (notify) {
      listener.globListReset();
    }
  }

  private void reloadGlobList() {
    globs.clear();
    if (showNullElement) {
      globs.add(null);
    }
    for (Glob glob : repository.getAll(type, matcher)) {
      globs.add(glob);
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

  public void setFilter(GlobMatcher matcher, final boolean reInit) {
    this.matcher = matcher;
    if (reInit) {
      initList(true);
    }
    else {
      GlobList from = getAll();
      this.matcher = matcher;
      reloadGlobList();
      GlobList to = getAll();
      GlobUtils.diff(from, to, new GlobUtils.DiffFunctor<Glob>() {
        public void add(Glob glob, int index) {
          listener.globInserted(index);
        }

        public void remove(int index) {
          listener.globRemoved(index);
        }

        public void move(int previousIndex, int newIndex) {
          listener.globMoved(previousIndex, newIndex);
        }
      });
    }
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
        MajorChanges visitor = new MajorChanges(changeSet, repository);
        visitor.complete();
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
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
            listener.globMoved(previousIndex, newIndex);
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

  private class MajorChanges {
    private final GlobRepository repository;
    private GlobList toAdd = new GlobList();
    private GlobList toRemove = new GlobList();

    public MajorChanges(ChangeSet set, GlobRepository repository) {
      this.repository = repository;
      Set<Key> updated = set.getUpdated(type);
      Set<Key> deleted = set.getDeleted(type);
      Set<Key> created = set.getCreated(type);
      for (Key key : created) {
        Glob glob = repository.find(key);
        if ((glob != null) && matcher.matches(glob, repository)) {
          toAdd.add(glob);
        }
      }
      for (Glob glob : globs) {
        if (updated.remove(glob.getKey())) {
          toRemove.add(glob);
          toAdd.add(repository.get(glob.getKey()));
        }
        else if (deleted.remove(glob.getKey())) {
          toRemove.add(glob);
        }
        else if (deleted.size() == 0 && updated.size() == 0) {
          break;
        }
      }
      for (Key key : updated) {
        toAdd.add(repository.get(key));
      }
    }

    public void complete() {
      if (toAdd.isEmpty() && toRemove.isEmpty()) {
        return;
      }
      Set<Glob> newList = new HashSet<Glob>(globs.size());
      for (Glob glob : globs) {
        newList.add(glob);
      }
      for (Glob glob : toRemove) {
        newList.remove(glob);
      }

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

