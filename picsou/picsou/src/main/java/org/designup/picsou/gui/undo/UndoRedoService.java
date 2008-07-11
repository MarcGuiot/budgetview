package org.designup.picsou.gui.undo;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoRedoService {
  private GlobRepository repository;
  private Stack<ChangeSet> changesToUndo = new Stack<ChangeSet>();
  private Stack<ChangeSet> changesToRedo = new Stack<ChangeSet>();
  private List<Listener> listeners = new ArrayList<Listener>();
  private boolean undoRedoInProgress = false;

  public interface Listener {
    void update();
  }

  public UndoRedoService(GlobRepository repository) {
    this.repository = repository;
    this.repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!undoRedoInProgress) {
          changesToUndo.push(changeSet);
          notifyListeners();
        }
      }

      public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
        reset();
      }
    });
  }

  private void reset() {
    changesToUndo.clear();
    changesToRedo.clear();
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
      ChangeSet original = changesToUndo.pop();
      repository.apply(original.reverse());
      changesToRedo.push(original);
      notifyListeners();
    }
    finally {
      undoRedoInProgress = false;
    }
  }

  public void redo() {
    undoRedoInProgress = true;
    try {
      ChangeSet original = changesToRedo.pop();
      repository.apply(original);
      changesToUndo.push(original);
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
}
