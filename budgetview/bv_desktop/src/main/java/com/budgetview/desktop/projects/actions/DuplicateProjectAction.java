package com.budgetview.desktop.projects.actions;

import com.budgetview.desktop.projects.components.DuplicateProjectDialog;
import com.budgetview.model.Project;
import com.budgetview.model.ProjectItem;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DuplicateProjectAction extends SingleSelectionAction implements Disposable {
  private final TypeChangeSetListener changeListener;
  private Key currentSelection;

  public DuplicateProjectAction(GlobRepository repository, Directory directory) {
    super(Lang.get("projectEdition.duplicate.menu"), Project.TYPE, repository, directory);
    changeListener = new TypeChangeSetListener(ProjectItem.TYPE) {
      public void update(GlobRepository repository) {
        doUpdate();
      }
    };
    repository.addChangeListener(changeListener);
    doUpdate();
  }

  protected void processSelection(Glob project) {
    this.currentSelection = project != null ? project.getKey() : null;
    doUpdate();
  }

  private void doUpdate() {
    Glob project = repository.find(currentSelection);
    setEnabled(project != null && repository.contains(ProjectItem.TYPE, GlobMatchers.linkedTo(project, ProjectItem.PROJECT)));
  }

  protected void process(Glob project, GlobRepository repository, Directory directory) {
    DuplicateProjectDialog dialog = new DuplicateProjectDialog(project, repository, directory, directory.get(JFrame.class));
    dialog.show();
  }

  public void dispose() {
    repository.removeChangeListener(changeListener);
  }
}
