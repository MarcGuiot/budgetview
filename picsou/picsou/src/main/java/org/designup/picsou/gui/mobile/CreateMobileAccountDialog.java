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
import org.globsframework.utils.Strings;
import org.globsframework.gui.GlobsPanelBuilder;
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

public class CreateMobileAccountDialog {
  private LocalGlobRepository localRepository;
  private final DefaultDirectory localDirectory;
  private final SelectionService selectionService;

  private PicsouDialog dialog;
  private JEditorPane message;
  private ProgressPanel progressBar;
  private Future<String> submit;
  private JTextField emailField;
  private JTextField passwordField;

  public CreateMobileAccountDialog(Directory directory, GlobRepository parentRepository) {
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
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/createMobileAccountDialog.splits",
                                                      localRepository, localDirectory);

    ValidateCreateMobileAccountAction validateAction = new ValidateCreateMobileAccountAction();
    JButton createButton = new JButton(validateAction);
    builder.add("create", createButton);

    emailField = builder.addEditor("email", UserPreferences.MAIL_FOR_MOBILE).getComponent();
    emailField.addActionListener(validateAction);
    passwordField = builder.addEditor("password", UserPreferences.PASSWORD_FOR_MOBILE).getComponent();
    passwordField.addActionListener(validateAction);

    message = Gui.createHtmlDisplay();
    builder.add("message", message);

    progressBar = new ProgressPanel();
    builder.add("progress", progressBar);

    builder.add("gotoGooglePlay", new BrowsingAction(localDirectory) {
      protected String getUrl() {
        return Lang.get("mobile.dialog.app.url");
      }
    });

    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.setFocusTraversalPolicy(
      new CustomFocusTraversalPolicy(emailField, passwordField, createButton));
  }

  public void show() {
    selectionService.select(UserPreferences.get(localRepository));
    dialog.pack();
    emailField.requestFocus();
    dialog.showCentered();
  }

  private class ValidateCreateMobileAccountAction extends AbstractAction {

    public ValidateCreateMobileAccountAction() {
      super(Lang.get("mobile.user.create.button"));
    }

    public void actionPerformed(ActionEvent e) {
      if (submit != null) {
        return;
      }
      if (!checkFieldsAreValid()) {
        return;
      }
      progressBar.start();
      submit = localDirectory.get(ExecutorService.class)
        .submit(new Callable<String>() {
          public String call() throws Exception {
            Ref<String> messageRef = new Ref<String>();
            boolean isOk = localDirectory.get(ConfigService.class)
              .createMobileAccount(UserPreferences.get(localRepository).get(UserPreferences.MAIL_FOR_MOBILE),
                                   messageRef);
            progressBar.stop();
            if (dialog.isVisible()) {
              localRepository.commitChanges(true);
              if (isOk) {
                dialog.setVisible(false);
              }
              else {
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

    String password = passwordField.getText();
    if (Strings.isNullOrEmpty(password)) {
      ErrorTip.show(passwordField, Lang.get("mobile.password.empty"), localDirectory, TipPosition.TOP_LEFT);
      return false;
    }

    return true;
  }

}