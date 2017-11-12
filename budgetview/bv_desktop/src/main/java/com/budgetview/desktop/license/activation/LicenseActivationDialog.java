package com.budgetview.desktop.license.activation;

import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.components.utils.CustomFocusTraversalPolicy;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.undo.UndoRedoService;
import com.budgetview.desktop.userconfig.UserConfigService;
import com.budgetview.model.LicenseActivationState;
import com.budgetview.model.User;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.FieldChangeListener;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;

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

    dialog = PicsouDialog.create(this, parent, directory);

    Glob user = localRepository.get(User.KEY);
    builder = new GlobsPanelBuilder(getClass(), "/layout/license/activation/licenseActivationDialog.splits",
                                    localRepository, this.localDirectory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        if ("newCode".equals(href)) {
          final String mail = localRepository.get(User.KEY).get(User.EMAIL);
          if (Strings.isNotEmpty(mail)) {
            directory.get(ExecutorService.class)
              .submit(new Runnable() {
                public void run() {
                  final String response = directory.get(UserConfigService.class).sendNewCodeRequest(mail);
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

    localRepository.addChangeListener(new FieldChangeListener(User.ACTIVATION_CODE) {
      public void update() {
        activateAction.setEnabled(localRepository.contains(User.KEY) &&
                                  Strings.isNotEmpty(localRepository.get(User.KEY).get(User.ACTIVATION_CODE)));
      }
    });

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

    clearActivationErrorMessage();

    initRegisterChangeListener();
    dialog.pack();
  }

  private void updateSendNewCodeMessage(Glob user) {
    String mail = user.get(User.EMAIL);
    if (Strings.isNullOrEmpty(mail)) {
      askForNewCodeMessage.setText(Lang.get("license.askForCode.noMail"));
    }
    else {
      askForNewCodeMessage.setText(Lang.get("license.askForCode"));
    }
  }

  private void initRegisterChangeListener() {
    changeSetListener = new AbstractChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)) {
          connectionMessage.setVisible(false);
          Glob user = repository.get(User.KEY);
          activationState = LicenseActivationState.get(user.get(User.LICENSE_ACTIVATION_STATE));
          if (activationState != null) {
            switch (activationState) {
              case ACTIVATION_IN_PROGRESS:
                break;
              case ACTIVATION_OK:
                showConfirmationPanel();
                break;
              case ACTIVATION_FAILED_CAN_NOT_CONNECT:
                showActivationErrorMessage("license.connect");
                break;
              case ACTIVATION_FAILED_MAIL_UNKNOWN:
                showActivationErrorMessage("license.mail.unknown");
                break;
              case ACTIVATION_FAILED_MAIL_SENT:
                showActivationErrorMessage("license.activation.failed.mailSent", localRepository.get(User.KEY).get(User.EMAIL));
                break;
              default:
                showActivationErrorMessage("license.activation.failed");
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

  private void clearActivationErrorMessage() {
    connectionMessage.setText(null);
    connectionMessage.setVisible(true);
  }

  private void showActivationErrorMessage(String message, String... args) {
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
    clearActivationErrorMessage();
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
          showConfirmationPanel();
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

  private void showConfirmationPanel() {
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
