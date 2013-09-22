package org.designup.picsou.gui.projects.components;

import org.designup.picsou.gui.components.images.GlobImageActions;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.gui.projects.actions.DeleteProjectAction;
import org.designup.picsou.model.Project;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectPopupMenuFactory implements PopupMenuFactory, Disposable {

  private ToggleBooleanAction activateAction;
  private DeleteProjectAction deleteAction;
  private GlobImageActions imageActions;

  private DisposableGroup disposables = new DisposableGroup();
  private SelectionService selectionService;
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
    deleteAction.setKey(projectKey);
  }

  public JPopupMenu createPopup() {
    if (menu != null) return menu;
    menu = new JPopupMenu();
    menu.add(activateAction);
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
