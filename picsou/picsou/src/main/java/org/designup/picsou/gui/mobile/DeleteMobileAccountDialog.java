package org.designup.picsou.gui.mobile;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.CloseDialogAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.components.utils.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DeleteMobileAccountDialog {
  private LocalGlobRepository localRepository;
  private final DefaultDirectory localDirectory;
  private final SelectionService selectionService;

  private PicsouDialog dialog;
  private JEditorPane message;
  private ProgressPanel progressBar;
  private JTextField emailField;
  private JTextField passwordField;
  private CardHandler cards;
  private ValidateDeleteMobileAccountAction validateAction;

  public DeleteMobileAccountDialog(Directory directory, GlobRepository parentRepository) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(UserPreferences.TYPE)
        .get();
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(SelectionService.class, selectionService);
    createDialog();
  }

  private void createDialog() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/deleteMobileAccountDialog.splits",
                                                      localRepository, localDirectory);

    validateAction = new ValidateDeleteMobileAccountAction();
    JButton createButton = new JButton(validateAction);
    builder.add("delete", createButton);

    cards = builder.addCardHandler("cards");

    emailField = builder.addEditor("emailField", UserPreferences.MAIL_FOR_MOBILE).getComponent();
    passwordField = builder.addEditor("passwordField", UserPreferences.PASSWORD_FOR_MOBILE).getComponent();

    message = Gui.createHtmlDisplay();
    builder.add("message", message);

    progressBar = new ProgressPanel();
    builder.add("progress", progressBar);

    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));
    dialog.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(emailField, createButton));
  }

  public void show() {
    selectionService.select(UserPreferences.get(localRepository));
    dialog.pack();
    emailField.requestFocus();
    dialog.showCentered();
  }


  private class ValidateDeleteMobileAccountAction extends AbstractAction {

    private Future<String> submit;
    private boolean submitInProgress = false;

    public ValidateDeleteMobileAccountAction() {
      super(Lang.get("mobile.user.delete.button"));
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
              .deleteMobileAccount(UserPreferences.get(localRepository).get(UserPreferences.MAIL_FOR_MOBILE),
                                   UserPreferences.get(localRepository).get(UserPreferences.PASSWORD_FOR_MOBILE),
                                   messageRef);

            progressBar.stop();
            if (dialog.isVisible()) {
              if (isOk) {
                localRepository.update(UserPreferences.get(localRepository).getKey(),
                                       FieldValue.value(UserPreferences.MAIL_FOR_MOBILE, null),
                                       FieldValue.value(UserPreferences.PASSWORD_FOR_MOBILE, null));
                localRepository.commitChanges(false);
                cards.show("confirmation");
              }
              else {
                localRepository.commitChanges(false);
                message.setText(messageRef.get());
              }
            }
            return null;
          }
        });
    }
  }

  private boolean checkFieldsAreValid() {
    String email = emailField.getText();
    if (Strings.isNullOrEmpty(email) || !email.contains("@")) {
      ErrorTip.show(emailField, Lang.get("mobile.mail.empty"), localDirectory, TipPosition.TOP_LEFT);
      return false;
    }
    return true;
  }

  private class CloseAction extends CloseDialogAction {
    public CloseAction(JDialog dialog) {
      super(dialog);
    }

    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      localRepository.dispose();
    }
  }
}
