package org.designup.picsou.gui.license.activation;

import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.utils.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.model.LicenseActivationState;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;

import static org.globsframework.model.FieldValue.value;

public class LicenseActivationDialog {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private ChangeSetListener changeSetListener;
  private SelectionService selectionService;

  private JEditorPane connectionMessage = new JEditorPane();
  private JEditorPane askForNewCodeMessage = new JEditorPane();
  private ProgressPanel progressPanel = new ProgressPanel();
  private ActivateAction activateAction = new ActivateAction();
  private LicenseActivationState activationState;
  private GlobsPanelBuilder builder;
  private GlobTextEditor mailEditor;
  private final CloseAction closeAction;

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
    builder = new GlobsPanelBuilder(getClass(), "/layout/license/activation/licenseActivationDialog.splits",
                                    localRepository, this.localDirectory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog) {
      protected void processCustomLink(String href) {
        if ("newCode".equals(href)) {
          final String mail = localRepository.get(User.KEY).get(User.EMAIL);
          if (Strings.isNotEmpty(mail)) {
            directory.get(ExecutorService.class)
              .submit(new Runnable() {
                public void run() {
                  final String response = directory.get(ConfigService.class).askForNewCodeByMail(mail);
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      connectionMessage.setText(response);
                      connectionMessage.setVisible(true);
                    }
                  });
                }
              });
          }
        }
      }
    });
    mailEditor = builder.addEditor("ref-mail", User.EMAIL).setNotifyOnKeyPressed(true);
    GlobTextEditor codeEditor = builder.addEditor("ref-code", User.ACTIVATION_CODE)
      .setValidationAction(activateAction)
      .setNotifyOnKeyPressed(true);

    GuiUtils.initHtmlComponent(askForNewCodeMessage);
    builder.add("messageSendNewCode", askForNewCodeMessage);
    updateSendNewCodeMessage(user);

    JButton activate = new JButton(activateAction);
    builder.add("activateCode", activate);

    dialog.setFocusTraversalPolicy(
      new CustomFocusTraversalPolicy(mailEditor.getComponent(), codeEditor.getComponent(), activate));

    GuiUtils.initHtmlComponent(connectionMessage);
    connectionMessage.setText(Lang.get("license.connect"));
    builder.add("connectionMessage", connectionMessage);
    builder.add("connectionState", progressPanel);

    closeAction = new CloseAction();
    dialog.addPanelWithButton(builder.<JPanel>load(), closeAction);
    dialog.setCloseAction(closeAction);

    Boolean isConnected = user.isTrue(User.CONNECTED);
    connectionMessage.setVisible(!isConnected);
    askForNewCodeMessage.setVisible(isConnected);

    initRegisterChangeListener();
    dialog.pack();
  }

  private void updateSendNewCodeMessage(Glob user) {
    String mail = user.get(User.EMAIL);
    if (Strings.isNullOrEmpty(mail)) {
      askForNewCodeMessage.setText(Lang.get("license.askForCode.noMail"));
    }
    else {
      askForNewCodeMessage.setText(Lang.get("license.askForCode", mail));
    }
  }

  private void initRegisterChangeListener() {
    changeSetListener = new AbstractChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          boolean isConnected = updateConnectionState(repository);
          if (!isConnected) {
            return;
          }
          if (changeSet.containsChanges(User.KEY, User.CONNECTED)) {
            selectionService.select(localRepository.get(User.KEY));
            activateAction.setEnabled(true);
          }
          activateAction.setEnabled(true);
          connectionMessage.setVisible(false);
          Glob user = repository.get(User.KEY);
          activationState = LicenseActivationState.get(user.get(User.LICENSE_ACTIVATION_STATE));
          if (activationState != null) {
            switch (activationState) {
              case ACTIVATION_IN_PROGRESS:
                break;
              case ACTIVATION_OK:
                showConfirmation();
                break;
              case ACTIVATION_FAILED_MAIL_UNKNOWN:
                updateDialogState("license.mail.unknown");
                break;
              case ACTIVATION_FAILED_MAIL_SENT:
                updateDialogState("license.activation.failed.mailSent", localRepository.get(User.KEY).get(User.EMAIL));
                break;
              default:
                updateDialogState("license.activation.failed");
            }
          }
        }
      }
    };
    repository.addChangeListener(changeSetListener);

    localRepository.addChangeListener(new AbstractChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          Glob user = repository.get(User.KEY);
          updateSendNewCodeMessage(user);
        }
      }
    });
  }

  private boolean updateConnectionState(GlobRepository repository) {
    boolean isConnected = repository.get(User.KEY).isTrue(User.CONNECTED);
    if (!isConnected) {
      connectionMessage.setText(Lang.get("license.connect"));
      connectionMessage.setVisible(true);
      askForNewCodeMessage.setVisible(false);
      selectionService.clear(User.TYPE);
      activateAction.setEnabled(false);
      progressPanel.stop();
    }
    return isConnected;
  }

  private void updateDialogState(String message, String... args) {
    localRepository.rollback();
    localRepository.update(User.KEY, User.ACTIVATION_CODE, null);
    selectionService.select(localRepository.get(User.KEY));
    connectionMessage.setText(Lang.get(message, args));
    connectionMessage.setVisible(true);
    progressPanel.stop();
    askForNewCodeMessage.setVisible(true);
  }

  public void show() {
    localRepository.rollback();
    localRepository.update(User.KEY, User.ACTIVATION_CODE, null);
    localRepository.update(User.KEY, User.SIGNATURE, null);
    selectionService.select(localRepository.get(User.KEY));
    updateConnectionState(localRepository);
    mailEditor.getComponent().requestFocus();
    dialog.showCentered();
    builder.dispose();
    progressPanel.stop();
  }

  public void showExpiration() {
    show();
  }

  private class ActivateAction extends AbstractAction {
    public ActivateAction() {
      super(Lang.get("license.activate"));
    }

    public void actionPerformed(ActionEvent e) {
      {
        Utils.beginRemove();
        Glob user = localRepository.get(User.KEY);
        if ("admin".equals(user.get(User.EMAIL))) {
          localRepository.update(User.KEY, User.IS_REGISTERED_USER, true);
          localRepository.commitChanges(false);
          localDirectory.get(UndoRedoService.class).cleanUndo();
          showConfirmation();
          return;
        }
        Utils.endRemove();
      }
      if (checkContainsValidChange()) {
        localRepository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.ACTIVATION_IN_PROGRESS.getId());
        progressPanel.start();
        localRepository.commitChanges(false);
        localDirectory.get(UndoRedoService.class).cleanUndo();
      }
    }

    private boolean checkContainsValidChange() {
      return (localRepository.getCurrentChanges().containsUpdates(User.ACTIVATION_CODE)
              || localRepository.getCurrentChanges().containsUpdates(User.EMAIL))
             && (Strings.isNotEmpty(localRepository.get(User.KEY).get(User.ACTIVATION_CODE)))
             && (Strings.isNotEmpty(localRepository.get(User.KEY).get(User.EMAIL)));
    }
  }

  private void showConfirmation() {
    LicenseConfirmationFeedbackPanel confirmationPanel = new LicenseConfirmationFeedbackPanel(localRepository, localDirectory);
    confirmationPanel.install(dialog, localRepository.get(User.KEY).get(User.EMAIL), closeAction);
  }

  private class CloseAction extends AbstractAction {
    public CloseAction() {
      super(Lang.get("close"));
    }

    public void actionPerformed(ActionEvent e) {
      if (dialog == null) {
        return;
      }
      dialog.setVisible(false);
      repository.removeChangeListener(changeSetListener);
      changeSetListener = null;
      localRepository.dispose();
      localRepository = null;
      dialog.dispose();
      dialog = null;
    }
  }
}
