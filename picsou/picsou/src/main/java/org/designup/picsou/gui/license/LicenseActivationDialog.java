package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class LicenseActivationDialog {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private ChangeSetListener changeSetListener;
  private SelectionService selectionService;

  private JEditorPane connectionMessage = new JEditorPane();
  private JLabel expirationLabel = new JLabel();
  private JProgressBar connectionState = new JProgressBar();
  private ValidateAction validateAction = new ValidateAction();
  private Integer activationState;

  public LicenseActivationDialog(Window parent, GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(selectionService);
    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(User.TYPE, UserPreferences.TYPE)
        .get();

    dialog = PicsouDialog.create(parent, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/licenseActivationDialog.splits",
                                                      localRepository, this.localDirectory);
    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog));
    builder.addEditor("mail", User.MAIL).setNotifyOnKeyPressed(true);
    builder.addEditor("code", User.ACTIVATION_CODE)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true);
    GuiUtils.initHtmlComponent(connectionMessage);
    connectionMessage.setText(Lang.get("license.connect"));
    builder.add("connectionMessage", connectionMessage);
    builder.add("connectionState", connectionState);

    builder.add("expirationLabel", expirationLabel);

    dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction());

    Boolean isConnected = localRepository.get(User.KEY).get(User.CONNECTED);
    connectionMessage.setVisible(!isConnected);

    initRegisterChangeListener();
    dialog.pack();
  }

  private void initRegisterChangeListener() {
    changeSetListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          Boolean isConnected = repository.get(User.KEY).get(User.CONNECTED);
          if (!isConnected) {
            connectionMessage.setText(Lang.get("license.connect"));
          }
          connectionMessage.setVisible(!isConnected);
          if (isConnected) {
            selectionService.select(localRepository.get(User.KEY));
            selectionService.select(localRepository.get(UserPreferences.KEY));
          }
          activationState = repository.get(User.KEY).get(User.ACTIVATION_STATE);
          if (activationState != null) {
            if (activationState == User.ACTIVATION_OK) {
              dialog.setVisible(false);
              repository.removeChangeListener(changeSetListener);
              localRepository.dispose();
            }
            else if (activationState == User.ACTIVATION_FAILED_MAIL_SENT) {
              updateDialogState("license.activation.faileded.mailSent", localRepository.get(User.KEY).get(User.MAIL));
            }
            else if (activationState != User.ACTIVATION_IN_PROGRESS) {
              updateDialogState("license.activation.faileded");
            }
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    };
    repository.addChangeListener(changeSetListener);
  }

  private void updateDialogState(String message, String... args) {
    localRepository.rollback();
    connectionMessage.setText(Lang.get(message, args));
    connectionMessage.setVisible(true);
    connectionState.setVisible(false);
    validateAction.setEnabled(true);
  }

  public void show(boolean expiration) {
    expirationLabel.setVisible(expiration);
    localRepository.rollback();
    if (repository.get(User.KEY).get(User.CONNECTED)) {
//      localRepository.update(UserPreferences.KEY, UserPreferences.FUTURE_MONTH_COUNT, 24);
      selectionService.select(localRepository.get(User.KEY));
      selectionService.select(localRepository.get(UserPreferences.KEY));
    }
    GuiUtils.center(dialog);
    dialog.setVisible(true);
  }

  public void showExpiration() {
    show(true);
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      setEnabled(false);
      Utils.beginRemove();
      Glob user = localRepository.get(User.KEY);
      if ("admin".equals(user.get(User.MAIL))) {
        localRepository.update(User.KEY, User.IS_REGISTERED_USER, true);
        localRepository.commitChanges(false);
        localDirectory.get(UndoRedoService.class).cleanUndo();
        dialog.setVisible(false);
        return;
      }
      Utils.endRemove();
      localRepository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_IN_PROGRESS);
      if (checkContainsValidChange()) {
        connectionState.setIndeterminate(true);
        connectionState.setVisible(true);
        localRepository.commitChanges(false);
        localDirectory.get(UndoRedoService.class).cleanUndo();
      }
    }

    private boolean checkContainsValidChange() {
      return (localRepository.getCurrentChanges().containsUpdates(User.ACTIVATION_CODE)
              || localRepository.getCurrentChanges().containsUpdates(User.MAIL))
             && (Strings.isNotEmpty(localRepository.get(User.KEY).get(User.ACTIVATION_CODE)))
             && (Strings.isNotEmpty(localRepository.get(User.KEY).get(User.MAIL)));
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