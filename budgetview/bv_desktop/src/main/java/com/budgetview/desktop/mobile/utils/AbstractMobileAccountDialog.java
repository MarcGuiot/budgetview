package com.budgetview.desktop.mobile.utils;

import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.CloseDialogAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.mobile.MobileService;
import com.budgetview.desktop.mobile.SendMobileDataAction;
import com.budgetview.model.UserPreferences;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AbstractMobileAccountDialog {
  protected final LocalGlobRepository localRepository;
  protected final DefaultDirectory localDirectory;
  protected final SelectionService selectionService;
  private GlobRepository parentRepository;

  public AbstractMobileAccountDialog(GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(UserPreferences.TYPE)
        .get();
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(SelectionService.class, selectionService);
  }

  protected void changePassword() {
    UserPreferences.initMobilePassword(localRepository, true);
  }

  protected class ActivateMobileAccountAction extends AbstractAction {

    private Future<String> submit;
    private boolean submitInProgress = false;
    private final PicsouDialog dialog;
    private ProgressPanel progressBar;

    public ActivateMobileAccountAction(PicsouDialog dialog, ProgressPanel progressBar) {
      super(Lang.get("mobile.user.create.button"));
      this.dialog = dialog;
      this.progressBar = progressBar;
    }

    public void actionPerformed(ActionEvent e) {
      if (submitInProgress ||
          (submit != null) ||
          !checkFieldsAreValid()) {
        return;
      }

      submitInProgress = true;
      progressBar.start();
      submit = localDirectory.get(ExecutorService.class)
        .submit(new Callable<String>() {
          public String call() throws Exception {
            Ref<String> messageRef = new Ref<String>();
            String mail = UserPreferences.get(localRepository).get(UserPreferences.MAIL_FOR_MOBILE);
            String password = UserPreferences.get(localRepository).get(UserPreferences.PASSWORD_FOR_MOBILE);
            boolean isOk = localDirectory.get(MobileService.class)
              .createMobileAccount(mail, password, messageRef, localRepository);
            SwingUtilities.invokeAndWait(new ShowCompletionStatus(isOk, messageRef));
            return null;
          }
        });
    }

    private class ShowCompletionStatus implements Runnable {
      private final boolean ok;
      private final Ref<String> messageRef;

      public ShowCompletionStatus(boolean ok, Ref<String> messageRef) {
        this.ok = ok;
        this.messageRef = messageRef;
      }

      public void run() {
        progressBar.stop();
        if (dialog.isVisible()) {
          localRepository.commitChanges(false);
          if (ok) {
            showActivationConfirmation();
            localDirectory.get(ExecutorService.class)
              .submit(new Callable<Object>() {
                public Object call() throws Exception {
                  Ref<String> msg = new Ref<String>();
                  SendMobileDataAction.sendToMobile(parentRepository, localDirectory, msg, true);
                  return msg.get();
                }
              });
          }
          else {
            showErrorMessage(messageRef.get());
          }
        }
      }
    }
  }
  protected abstract void showErrorMessage(String errorMessage);

  protected abstract void showActivationConfirmation();

  protected abstract boolean checkFieldsAreValid();

  protected class CloseAction extends CloseDialogAction {
    public CloseAction(JDialog dialog) {
      super(dialog);
    }

    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      localRepository.dispose();
    }
  }
}
