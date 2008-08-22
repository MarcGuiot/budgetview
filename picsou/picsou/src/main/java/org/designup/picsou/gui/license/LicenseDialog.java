package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class LicenseDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private SelectionService selectionService;
  private JLabel connectMessageLabel;
  private ChangeSetListener changeSetListener;
  private GlobRepository repository;
  private Directory localDirectory;
  private Integer activationState;
  private JProgressBar progressBar;
  private JLabel registrationMessageLabel;
  private LicenseDialog.ValidAction validAction;

  public LicenseDialog(Window parent, GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    this.localDirectory.add(selectionService);
    LocalGlobRepositoryBuilder localGlobRepositoryBuilder = LocalGlobRepositoryBuilder.init(repository)
      .copy(User.TYPE, UserPreferences.TYPE);
    this.localRepository = localGlobRepositoryBuilder.get();
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/LicenseDialog.splits",
                                                      localRepository, this.localDirectory);
    builder.addEditor("mail", User.MAIL);
    builder.addEditor("code", User.ACTIVATION_CODE);
    builder.addEditor("monthCount", UserPreferences.FUTURE_MONTH_COUNT);
    connectMessageLabel = new JLabel(Lang.get("license.connect"));
    builder.add("connectionMessage", connectMessageLabel);
    registrationMessageLabel = new JLabel(Lang.get("license.connect"));
    builder.add("registrationMessage", registrationMessageLabel);
    registrationMessageLabel.setVisible(false);
    progressBar = new JProgressBar();
    builder.add("connectionState", progressBar);

    validAction = new ValidAction();
    dialog = PicsouDialog.createWithButtons(parent, builder.<JPanel>load(),
                                            validAction,
                                            new CancelAction());
    Boolean isConnected = localRepository.get(User.KEY).get(User.CONNECTED);
    connectMessageLabel.setVisible(!isConnected);

    changeSetListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          Boolean isConnected = repository.get(User.KEY).get(User.CONNECTED);
          connectMessageLabel.setVisible(!isConnected);
          if (isConnected) {
            selectionService.select(localRepository.get(User.KEY));
            selectionService.select(localRepository.get(UserPreferences.KEY));
          }
        }
        activationState = repository.get(User.KEY).get(User.ACTIVATION_STATE);
        if (activationState != null) {
          if (activationState == User.ACTIVATION_OK) {
            dialog.setVisible(false);
            repository.removeChangeListener(changeSetListener);
            localRepository.dispose();
          }
          else if (activationState != User.ACTIVATION_IN_PROCESS) {
            connectMessageLabel.setText(Lang.get("license.activation.fail"));
            connectMessageLabel.setVisible(true);
            progressBar.setVisible(false);
            validAction.setEnabled(true);
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    };
    repository.addChangeListener(changeSetListener);
    dialog.pack();
  }

  public void show() {
    localRepository.rollback();
    if (repository.get(User.KEY).get(User.CONNECTED)) {
      selectionService.select(localRepository.get(User.KEY));
      selectionService.select(localRepository.get(UserPreferences.KEY));
    }
    dialog.setVisible(true);
  }


  private class ValidAction extends AbstractAction {
    public ValidAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      setEnabled(false);
      Utils.beginRemove();
      Glob user = localRepository.get(User.KEY);
      if (user.get(User.MAIL).equals("admin")) {
        localRepository.update(UserPreferences.KEY, UserPreferences.REGISTRED_USER, true);
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
      Utils.endRemove();
      localRepository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_IN_PROCESS);
      localRepository.commitChanges(false);
      localDirectory.get(UndoRedoService.class).cleanUndo();
      progressBar.setVisible(true);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      repository.removeChangeListener(changeSetListener);
    }
  }
}
