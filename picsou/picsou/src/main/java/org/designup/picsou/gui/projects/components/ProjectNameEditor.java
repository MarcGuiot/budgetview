package org.designup.picsou.gui.projects.components;

import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.model.Project;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobBooleanNodeStyleUpdater;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ProjectNameEditor {

  private GlobRepository parentRepository;
  private LocalGlobRepository localRepository;
  private Directory directory;

  private PopupMenuFactory menuFactory;

  private JPanel panel;
  private CardHandler cards;
  private GlobTextEditor nameField;
  private GlobButtonView projectNameButton;
  private Key currentProjectKey;
  private GlobBooleanNodeStyleUpdater styleUpdater;
  private Listener listener;

  public interface Listener {
    void processEditShown(boolean shown);
  }

  public ProjectNameEditor(PopupMenuFactory menuFactory, GlobRepository parentRepository, Directory directory) {
    this.menuFactory = menuFactory;
    this.parentRepository = parentRepository;
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(Project.TYPE)
        .get();
    this.directory = directory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  public void setCurrentProject(Key projectKey) {
    this.currentProjectKey = projectKey;
    projectNameButton.forceSelection(currentProjectKey);
    nameField.forceSelection(currentProjectKey);
    if ((currentProjectKey != null) && Strings.isNullOrEmpty(parentRepository.get(currentProjectKey).get(Project.NAME))) {
      edit();
    }
    else {
      showReadonly();
    }
    styleUpdater.setKey(currentProjectKey);
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void edit() {
    localRepository.reset(new GlobList(parentRepository.get(currentProjectKey)), Project.TYPE);
    projectNameButton.forceSelection(currentProjectKey);
    nameField.forceSelection(currentProjectKey);
    showEdit();
    GuiUtils.selectAndRequestFocus(nameField.getComponent());
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/components/projectNameEditor.splits",
                                                      parentRepository, directory);

    cards = builder.addCardHandler("projectNameCards");

    PopupGlobFunctor functor = new PopupGlobFunctor(menuFactory);
    projectNameButton = GlobButtonView.init(Project.NAME, parentRepository, directory, functor);
    SplitsNode<JButton> projectNameNode = builder.add("projectNameButton", projectNameButton.getComponent());
    functor.setComponent(projectNameButton.getComponent());

    this.styleUpdater = new GlobBooleanNodeStyleUpdater(Project.ACTIVE, projectNameNode,
                                                        "activeProject", "inactiveProject",
                                                        parentRepository);

    nameField = builder.addEditor("projectNameField", Project.NAME);
    ValidateAction validate = new ValidateAction();
    builder.add("validate", validate);
    nameField.setValidationAction(validate);
    builder.add("cancel", new CancelEditionAction());
    cards.show("readonly");

    panel = builder.load();
  }

  private boolean check() {
    if (Strings.isNullOrEmpty(nameField.getComponent().getText())) {
      ErrorTip.show(nameField.getComponent(), Lang.get("projectEdition.error.noProjectName"), directory, TipPosition.TOP_LEFT);
      GuiUtils.selectAndRequestFocus(nameField.getComponent());
      return false;
    }
    return true;
  }

  protected class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("validate"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!check()) {
        return;
      }
      localRepository.commitChanges(false);
      showReadonly();
    }
  }

  protected class CancelEditionAction extends AbstractAction {

    public CancelEditionAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.rollback();
      showReadonly();
    }
  }

  private void showEdit() {
    cards.show("edit");
    notifyEditShown(true);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GuiUtils.selectAndRequestFocus(nameField.getComponent());
      }
    });
  }

  private void showReadonly() {
    cards.show("readonly");
    notifyEditShown(false);
  }

  private void notifyEditShown(boolean shown) {
    if (listener != null) {
      listener.processEditShown(shown);
    }
  }
}
