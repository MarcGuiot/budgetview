package org.designup.picsou.gui.mobile;

import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.components.utils.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.mobile.utils.AbstractMobileAccountDialog;
import org.designup.picsou.gui.mobile.utils.ConfirmMobileAccountPanel;
import org.designup.picsou.gui.mobile.utils.PasswordEditionPanel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class EditMobileAccountDialog extends AbstractMobileAccountDialog {

  private PicsouDialog dialog;
  private JEditorPane message;
  private ProgressPanel progressBar;
  private JTextField emailField;
  private CardHandler cards;

  public EditMobileAccountDialog(Directory directory, GlobRepository parentRepository) {
    super(parentRepository, directory);
    createDialog();
  }

  private void createDialog() {
    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);
    progressBar = new ProgressPanel();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/editMobileAccountDialog.splits",
                                                      localRepository, localDirectory);

    cards = builder.addCardHandler("cards");

    emailField = builder.addEditor("emailField", UserPreferences.MAIL_FOR_MOBILE).getComponent();
    builder.add("passwordEdition", new PasswordEditionPanel(localRepository, localDirectory).getPanel());

    message = Gui.createHtmlDisplay();
    builder.add("message", message);

    ActivateMobileAccountAction activateAction = new ActivateMobileAccountAction(dialog, progressBar);
    JButton activateButton = new JButton(activateAction);
    builder.add("activateMobileAccount", activateButton);
    emailField.addActionListener(activateAction);

    JButton deleteButton = new JButton(new DeleteMobileAccountAction());
    builder.add("delete", deleteButton);

    builder.add("progress", progressBar);

    ConfirmMobileAccountPanel confirmPanel = new ConfirmMobileAccountPanel(localRepository, localDirectory);
    builder.add("confirmMobileAccount", confirmPanel.getPanel());

    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));
    dialog.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(emailField, activateButton, deleteButton));
  }

  public void show() {
    Glob glob = UserPreferences.get(localRepository);

    selectionService.select(glob);
    dialog.pack();
    dialog.showCentered();
  }

  private class DeleteMobileAccountAction extends AbstractAction {

    private Future<String> submit;
    private boolean submitInProgress = false;

    public DeleteMobileAccountAction() {
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
                cards.show("confirmDeletion");
              }
              else {
                localRepository.commitChanges(false);
                showErrorMessage(messageRef.get());
              }
            }
            return null;
          }
        });
    }
  }

  protected void showActivationConfirmation() {
    cards.show("confirmActivation");
  }

  protected void showErrorMessage(String errorMessage) {
    message.setText(errorMessage);
  }

  protected boolean checkFieldsAreValid() {
    String email = this.emailField.getText();
    if (Strings.isNullOrEmpty(email) || !email.contains("@")) {
      ErrorTip.show(this.emailField, Lang.get("mobile.mail.empty"), localDirectory, TipPosition.TOP_LEFT);
      return false;
    }
    return true;
  }
}
