package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
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
  private JEditorPane messageSendCode = new JEditorPane();
  private JLabel expirationLabel = new JLabel();
  private JProgressBar connectionState = new JProgressBar();
  private ValidateAction validateAction = new ValidateAction();
  private Integer activationState;
  private GlobsPanelBuilder builder;
  private GlobTextEditor mailEditor;

  public LicenseActivationDialog(Window parent, GlobRepository repository, final Directory directory) {
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(selectionService);
    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(User.TYPE)
        .get();

    dialog = PicsouDialog.create(parent, directory);

    Glob user = localRepository.get(User.KEY);
    builder = new GlobsPanelBuilder(getClass(), "/layout/general/licenseActivationDialog.splits",
                                    localRepository, this.localDirectory);
    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog){
      protected void processCustomLink(String href) {
        if ("newCode".equals(href)) {
          final String mail = localRepository.get(User.KEY).get(User.MAIL);
          if (Strings.isNotEmpty(mail)) {
            Thread thread = new Thread() {
              public void run() {
                final String response = directory.get(ConfigService.class).askForNewCodeByMail(mail);
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    connectionMessage.setText(response);
                    connectionMessage.setVisible(true);
                  }
                });
              }
            };
            thread.setDaemon(true);
            thread.run();
          }
        }
      }
    });
    mailEditor = builder.addEditor("ref-mail", User.MAIL).setNotifyOnKeyPressed(true);
    builder.addEditor("ref-code", User.ACTIVATION_CODE)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true);
    GuiUtils.initHtmlComponent(messageSendCode);
    builder.add("messageSendNewCode", messageSendCode);
    updateSendNewCodeMessage(user);

    GuiUtils.initHtmlComponent(connectionMessage);
    connectionMessage.setText(Lang.get("license.connect"));
    builder.add("connectionMessage", connectionMessage);
    builder.add("connectionState", connectionState);

    builder.add("expirationLabel", expirationLabel);

    dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction());

    Boolean isConnected = user.isTrue(User.CONNECTED);
    connectionMessage.setVisible(!isConnected);
    messageSendCode.setVisible(isConnected);

    initRegisterChangeListener();
    dialog.pack();
  }

  private void updateSendNewCodeMessage(Glob user) {
    String mail = user.get(User.MAIL);
    if (Strings.isNullOrEmpty(mail)) {
      messageSendCode.setText(Lang.get("license.askForCode.noMail"));
    }
    else {
      messageSendCode.setText(Lang.get("license.askForCode", mail));
    }
  }

  private void initRegisterChangeListener() {
    changeSetListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          boolean isConnected = updateConnectionState(repository);
          if (!isConnected) {
            return;
          }
          if (changeSet.containsChanges(User.KEY, User.CONNECTED)) {
            selectionService.select(localRepository.get(User.KEY));
            validateAction.setEnabled(true);
          }
          validateAction.setEnabled(true);
          connectionMessage.setVisible(false);
          Glob user = repository.get(User.KEY);
          activationState = user.get(User.ACTIVATION_STATE);
          if (activationState != null) {
            if (activationState == User.ACTIVATION_OK) {
              dialog.setVisible(false);
              repository.removeChangeListener(changeSetListener);
              localRepository.dispose();
            }
            else if (activationState == User.ACTIVATION_FAILED_MAIL_SENT) {
              updateDialogState("license.activation.failed.mailSent", localRepository.get(User.KEY).get(User.MAIL));
            }
            else if (activationState == User.ACTIVATION_FAILED_MAIL_SENT) {
              updateDialogState("license.code.invalid", localRepository.get(User.KEY).get(User.MAIL));
            }
            else if (activationState == User.ACTIVATION_FAILED_MAIL_UNKNOWN) {
              updateDialogState("license.mail.unknown");
            }
            else if (activationState != User.ACTIVATION_IN_PROGRESS) {
              updateDialogState("license.activation.failed");
            }
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    };
    repository.addChangeListener(changeSetListener);

    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)){
          Glob user = repository.get(User.KEY);
          updateSendNewCodeMessage(user);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });
  }

  private boolean updateConnectionState(GlobRepository repository) {
    boolean isConnected = repository.get(User.KEY).isTrue(User.CONNECTED);
    if (!isConnected) {
      connectionMessage.setText(Lang.get("license.connect"));
      connectionMessage.setVisible(true);
      messageSendCode.setVisible(false);
      selectionService.clear(User.TYPE);
      validateAction.setEnabled(false);
    }
    return isConnected;
  }

  private void updateDialogState(String message, String... args) {
    localRepository.rollback();
    localRepository.update(User.KEY, User.ACTIVATION_CODE, null);
    selectionService.select(localRepository.get(User.KEY));
    connectionMessage.setText(Lang.get(message, args));
    connectionMessage.setVisible(true);
    connectionState.setVisible(false);
    messageSendCode.setVisible(true);
  }

  public void show(boolean expiration) {
    expirationLabel.setVisible(expiration);
    localRepository.rollback();
    localRepository.update(User.KEY, User.ACTIVATION_CODE, null);
    localRepository.update(User.KEY, User.SIGNATURE, null);
    selectionService.select(localRepository.get(User.KEY));
    updateConnectionState(localRepository);
    mailEditor.getComponent().requestFocus();
    dialog.showCentered(true);
    builder.dispose();
  }

  public void showExpiration() {
    show(true);
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      {
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
      }
      if (checkContainsValidChange()) {
        localRepository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_IN_PROGRESS);
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
