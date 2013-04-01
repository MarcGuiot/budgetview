package org.designup.picsou.gui.mobile.utils;

import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.CloseDialogAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
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

  public AbstractMobileAccountDialog(GlobRepository parentRepository, Directory directory) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(UserPreferences.TYPE)
        .get();
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(SelectionService.class, selectionService);
  }

  protected class ChangePasswordAction extends AbstractAction{
    public ChangePasswordAction() {
      super(Lang.get("mobile.user.generate.new.password"));
    }

    public void actionPerformed(ActionEvent e) {
      changePassword();
    }
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
            boolean isOk = localDirectory.get(ConfigService.class)
              .createMobileAccount(UserPreferences.get(localRepository).get(UserPreferences.MAIL_FOR_MOBILE),
                                   UserPreferences.get(localRepository).get(UserPreferences.PASSWORD_FOR_MOBILE),
                                   messageRef);

            progressBar.stop();
            if (dialog.isVisible()) {
              localRepository.commitChanges(false);
              if (isOk) {
                showActivationConfirmation();
              }
              else {
                showErrorMessage(messageRef.get());
              }
            }
            return null;
          }
        });
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
