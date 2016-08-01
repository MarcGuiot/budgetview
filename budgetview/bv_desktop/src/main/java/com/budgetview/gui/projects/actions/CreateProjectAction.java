package com.budgetview.gui.projects.actions;

import com.budgetview.gui.series.SeriesEditor;
import com.budgetview.gui.accounts.utils.AccountCreation;
import com.budgetview.model.AddOns;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class CreateProjectAction extends AbstractAction implements ChangeSetListener, Disposable {
  private GlobRepository repository;
  private Directory directory;


  public CreateProjectAction(GlobRepository repository, Directory directory) {
    super(Lang.get("projectView.create"));
    this.repository = repository;
    this.directory = directory;
  }

  public void setAutoHide() {
    repository.addChangeListener(this);
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(AddOns.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(AddOns.TYPE)) {
      update();
    }
  }

  private void update() {
    setEnabled(AddOns.isEnabled(AddOns.PROJECTS, repository));
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    if (!AccountCreation.containsUserAccount(repository, directory,
                                             Lang.get("accountCreation.projectCreation.message"))) {
      return;
    }

    SeriesEditor.get(directory).showNewProject();
  }
}
