package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

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
