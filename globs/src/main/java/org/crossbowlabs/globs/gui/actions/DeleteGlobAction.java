package org.crossbowlabs.globs.gui.actions;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeleteGlobAction extends AbstractAction implements GlobSelectionListener {

  private GlobList lastSelection = GlobList.EMPTY;
  private GlobType type;
  private GlobRepository repository;
  private Condition condition = Condition.ALL;

  public interface Condition {
    boolean accept(GlobList list);

    public static final Condition ALL = new Condition() {
      public boolean accept(GlobList list) {
        return true;
      }
    };
  }

  public DeleteGlobAction(String actionName, GlobType type, GlobRepository repository, Directory directory) {
    super(actionName);
    this.type = type;
    this.repository = repository;
    setEnabled(false);
    directory.get(SelectionService.class).addListener(this, type);
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public void selectionUpdated(GlobSelection selection) {
    lastSelection = selection.getAll(type);
    setEnabled(!lastSelection.isEmpty() && condition.accept(lastSelection));
  }

  public void actionPerformed(ActionEvent e) {
    repository.delete(lastSelection);
    lastSelection = GlobList.EMPTY;
    setEnabled(false);
  }
}
