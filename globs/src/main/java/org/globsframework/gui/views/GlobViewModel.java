package org.globsframework.gui.views;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.exceptions.ItemAlreadyExists;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GlobViewModel implements ChangeSetListener, Disposable {
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

    void startUpdate();

    void updateComplete();
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

  public void setShowNullElement(boolean shown) {
    if (showNullElement == shown) {
      return;
    }
    this.showNullElement = shown;
    reLoadAndCallListener(matcher);
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
      reLoadAndCallListener(matcher);
    }
  }

  private void reLoadAndCallListener(GlobMatcher matcher) {
    GlobList from = getAll();
    this.matcher = matcher;
    reloadGlobList();
    GlobList to = getAll();
    listener.startUpdate();
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
    listener.updateComplete();
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
    for (Iterator<Glob> iter = globs.iterator(); iter.hasNext(); ) {
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
//        if (globs.contains(glob)) {
//          throw new ItemAlreadyExists("Glob " + glob + " already present in " + globs);
//        }
        int index = globs.add(glob);
        listener.globInserted(index);
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      Glob glob = repository.get(key);
      boolean matches = matcher.matches(glob, repository);
      int previousIndex = globs.firstIndexOf(new GlobKeyMatcher(key), repository);
      boolean wasPresent = previousIndex >= 0;
      if (wasPresent && !matches) {
        globs.remove(previousIndex);
        listener.globRemoved(previousIndex);
      }
      else if (!wasPresent && matches) {
        int newIndex = globs.add(glob);
        listener.globInserted(newIndex);
      }
      else if (wasPresent) {
        globs.remove(previousIndex);
        int newIndex = globs.add(repository.get(key));
        if (newIndex == previousIndex) {
          listener.globUpdated(newIndex);
        }
        else {
          listener.globMoved(previousIndex, newIndex);
        }
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
        if (glob != null) {
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

      for (Iterator<Glob> iterator = newList.iterator(); iterator.hasNext(); ) {
        Glob glob = iterator.next();
        if ((glob != null) && !glob.exists()) {
          Log.write("Bug : " + glob.getKey() + " is deleted");
          iterator.remove();
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

  public static class NullListener implements GlobViewModel.Listener {
    public void globInserted(int index) {
    }

    public void globUpdated(int index) {
    }

    public void globRemoved(int index) {
    }

    public void globMoved(int previousIndex, int newIndex) {
    }

    public void globListPreReset() {
    }

    public void globListReset() {
    }

    public void startUpdate() {
    }

    public void updateComplete() {
    }
  }

}

