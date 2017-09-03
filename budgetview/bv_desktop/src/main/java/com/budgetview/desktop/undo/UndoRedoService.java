package com.budgetview.desktop.undo;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidState;

import java.io.PrintStream;
import java.util.*;

public class UndoRedoService {
  private GlobRepository repository;
  private LinkedList<Change> changesToUndo = new LinkedList<Change>();
  private Stack<Change> changesToRedo = new Stack<Change>();
  private List<Listener> listeners = new ArrayList<Listener>();
  private boolean undoRedoInProgress = false;
  private static final int MAX_UNDO = 20;

  public interface Listener {
    void update();
  }

  public interface Change {
    void apply();
    void revert();
  }

  public UndoRedoService(GlobRepository repository) {
    this.repository = repository;
    installChangeListener();
  }

  private void installChangeListener() {
    this.repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!undoRedoInProgress) {
          changesToUndo.addLast(new RepositoryChange(changeSet));
          if (changesToUndo.size() == MAX_UNDO) {
            changesToUndo.removeFirst();
          }
          changesToRedo.clear();
          notifyListeners();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        reset();
      }
    });
  }

  public void reset() {
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
      Change change = changesToUndo.removeLast();
      change.revert();
      changesToRedo.push(change);
      notifyListeners();
    }
    finally {
      undoRedoInProgress = false;
    }
  }

  public void removeLastUndo() {
    undoRedoInProgress = true;
    try {
      changesToUndo.removeLast();
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
      changesToUndo.addLast(change);
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

  public void cleanUndo() {
    changesToUndo.clear();
    notifyListeners();
  }

  public void appendToNextUndo(Change change) throws InvalidState {
    if (changesToUndo.isEmpty()) {
      throw new InvalidState("There is no change to undo in the stack");
    }

    Change current = changesToUndo.removeLast();
    changesToUndo.addLast(new CompositeChange(current, change));
    if (changesToUndo.size() == MAX_UNDO) {
      changesToUndo.removeFirst();
    }
  }

  public void dump(PrintStream out) {
    for (Change change : changesToUndo) {
      out.append(change.toString());
      out.append("\n--------------------------------\n");
    }
  }

  private class RepositoryChange implements Change {
    private ChangeSet changeSet;

    private RepositoryChange(ChangeSet changeSet) {
      this.changeSet = changeSet;
    }

    public void apply() {
      applyChanges(changeSet);
    }

    public void revert() {
      applyChanges(changeSet.reverse());
    }

    private void applyChanges(ChangeSet changeSet) {
      repository.startChangeSet();
      try {
        repository.apply(changeSet);
      }
      finally {
        repository.completeChangeSetWithoutTriggers();
      }
    }

    public String toString() {
      return changeSet.toString();
    }
  }

  private class CompositeChange implements Change {

    private Change change1;
    private Change change2;

    public CompositeChange(Change change1, Change change2) {
      this.change1 = change1;
      this.change2 = change2;
    }

    public void apply() {
      change1.apply();
      change2.apply();
    }

    public void revert() {
      change2.revert();
      change1.revert();
    }

    public String toString() {
      return "Combined: " + change1 + "\nand: " + change2;
    }
  }
}
