package com.budgetview.gui.projects.components;

import com.budgetview.gui.components.images.GlobImageActions;
import com.budgetview.gui.projects.ProjectView;
import com.budgetview.gui.projects.actions.DeleteProjectAction;
import com.budgetview.gui.projects.actions.DuplicateProjectAction;
import com.budgetview.model.Project;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectPopupMenuFactory implements PopupMenuFactory, Disposable {

  private ToggleBooleanAction activateAction;
  private DuplicateProjectAction duplicateAction;
  private DeleteProjectAction deleteAction;
  private GlobImageActions imageActions;

  private DisposableGroup disposables = new DisposableGroup();
  private boolean showMainActionsOnly;
  private JPopupMenu menu;

  public ProjectPopupMenuFactory(GlobRepository repository, Directory directory) {
    this(null, repository, directory);
  }

  public ProjectPopupMenuFactory(Key projectKey, GlobRepository repository, Directory directory) {

    activateAction = new ToggleBooleanAction(projectKey, Project.ACTIVE,
                                             Lang.get("projectEdition.setActive.textForTrue"),
                                             Lang.get("projectEdition.setActive.textForFalse"),
                                             repository);
    disposables.add(activateAction);

    duplicateAction = new DuplicateProjectAction(repository, directory);
    duplicateAction.setKey(projectKey);
    disposables.add(duplicateAction);

    imageActions = new GlobImageActions(projectKey, Project.PICTURE, repository, directory, ProjectView.MAX_PICTURE_SIZE);

    deleteAction = new DeleteProjectAction(projectKey, repository, directory);
    disposables.add(deleteAction);
  }

  public void setShowMainActionsOnly(boolean showMainActionsOnly) {
    this.showMainActionsOnly = showMainActionsOnly;
    this.menu = null;
  }

  public void updateSelection(Key projectKey) {
    imageActions.setKey(projectKey);
    activateAction.setKey(projectKey);
    duplicateAction.setKey(projectKey);
    deleteAction.setKey(projectKey);
  }

  public JPopupMenu createPopup() {
    if (menu != null) {
      return menu;
    }
    menu = new JPopupMenu();
    menu.add(activateAction);
    menu.add(duplicateAction);
    if (!showMainActionsOnly) {
      menu.addSeparator();
      imageActions.add(menu);
    }
    menu.addSeparator();
    menu.add(deleteAction);
    return menu;
  }

  public void dispose() {
    disposables.dispose();
  }
}
