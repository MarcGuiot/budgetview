package org.designup.picsou.gui.undo;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class UndoRedoService {
  private GlobRepository repository;
  private Stack<Change> changesToUndo = new Stack<Change>();
  private Stack<Change> changesToRedo = new Stack<Change>();
  private List<Listener> listeners = new ArrayList<Listener>();
  private boolean undoRedoInProgress = false;

  private final GlobType[] selectionTypes = {Card.TYPE, Month.TYPE, Category.TYPE, Transaction.TYPE};
  private final MultiMap<GlobType, Key> currentSelections = new MultiMap<GlobType, Key>();

  public interface Listener {
    void update();
  }

  public UndoRedoService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    installChangeListener();
    installSelectionListener(directory.get(SelectionService.class));
  }

  private void installChangeListener() {
    this.repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!undoRedoInProgress) {
          changesToUndo.push(createChange(changeSet));
          notifyListeners();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        reset();
      }
    });
  }

  private void installSelectionListener(SelectionService selectionService) {
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        for (GlobType type : selectionTypes) {
          if (selection.isRelevantForType(type)) {
            currentSelections.remove(type);
            final List<Key> keys = Arrays.asList(selection.getAll(type).getKeys());
            currentSelections.putAll(type, keys);
          }
        }
      }
    }, selectionTypes);
  }

  private void reset() {
    changesToUndo.clear();
    changesToRedo.clear();
    currentSelections.clear();
    notifyListeners();
  }

  public boolean isUndoAvailable() {
    return !changesToUndo.isEmpty();
  }

  public boolean isRedoAvailable() {
    return !changesToRedo.isEmpty();
  }

  public void undo() {
    undoRedoInProgress = true;
    try {
      Change change = changesToUndo.pop();
      change.revert();
      changesToRedo.push(change);
      notifyListeners();
    }
    finally {
      undoRedoInProgress = false;
    }
  }

  public void redo() {
    undoRedoInProgress = true;
    try {
      Change change = changesToRedo.pop();
      change.apply();
      changesToUndo.push(change);
      notifyListeners();
    }
    finally {
      undoRedoInProgress = false;
    }
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  private void notifyListeners() {
    for (Listener listener : listeners) {
      listener.update();
    }
  }

  private Change createChange(ChangeSet changeSet) {
    return new Change(changeSet, currentSelections);
  }

  private class Change {
    private ChangeSet changeSet;
    private final MultiMap<GlobType, Key> selections;

    private Change(ChangeSet changeSet, MultiMap<GlobType, Key> selections) {
      this.changeSet = changeSet;
      this.selections = selections.duplicate();
    }

    public void apply() {
      applyChanges(changeSet);
    }

    public void revert() {
      applyChanges(changeSet.reverse());
    }

    private void applyChanges(ChangeSet changeSet) {
      repository.enterBulkDispatchingMode();
      try {
        repository.apply(changeSet);
      }
      finally {
        repository.completeBulkDispatchingModeWithoutTriggers();
      }
    }
  }
}
