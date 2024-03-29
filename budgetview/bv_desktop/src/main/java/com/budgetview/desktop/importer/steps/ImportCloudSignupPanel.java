package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.User;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudSignupPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JEditorPane messageLabel;
  private Action nextAction;
  private JTextField emailField;
  private JLabel errorLabel;

  public ImportCloudSignupPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudSignupPanel.splits", repository, localDirectory);

    nextAction = new AbstractAction(Lang.get("import.cloud.signup.next")) {
      public void actionPerformed(ActionEvent e) {
        processNext();
      }
    };

    messageLabel = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("messageLabel", messageLabel);

    emailField = new JTextField();
    builder.add("emailField", emailField);
    emailField.setAction(nextAction);

    errorLabel = new JLabel(" ");
    builder.add("errorLabel", errorLabel);
    errorLabel.setVisible(false);

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(getCloseLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  private void processNext() {

    final String email = Strings.trim(emailField.getText());

    if (Strings.isNullOrEmpty(email)) {
      errorLabel.setText(Lang.get("import.cloud.signup.missing.email"));
      errorLabel.setVisible(true);
      return;
    }

    if (!Strings.looksLikeAnEmail(email)) {
      errorLabel.setText(Lang.get("import.cloud.signup.invalid.email"));
      errorLabel.setVisible(true);
      return;
    }

    setAllEnabled(false);
    progressPanel.start();
    final boolean isSignup = !CloudDesktopUser.isRegistered(repository);
    CloudService.Callback callback = new CloudService.Callback() {
      public void processCompletion() {
        if (isSignup) {
          controller.showCloudValidationForSignup(email);
        }
        else {
          controller.showCloudValidationForEmailModification(email);
        }
        progressPanel.stop();
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(email, status);
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
        progressPanel.stop();
      }
    };
    if (isSignup) {
      cloudService.signup(email, repository, callback);
    }
    else {
      cloudService.modifyEmailAddress(email, repository, callback);
    }
  }

  private void setAllEnabled(boolean enabled) {
    nextAction.setEnabled(enabled);
    emailField.setEnabled(enabled);
  }

  public void prepareForDisplay() {
    setAllEnabled(true);
    if (CloudDesktopUser.isRegistered(repository)) {
      messageLabel.setText(Lang.get("import.cloud.modifyEmailAddress.message"));
    }
    else {
      emailField.setText(getDefaultEmail());
      messageLabel.setText(Lang.get("import.cloud.signup.message"));
    }
    GuiUtils.selectAndRequestFocus(emailField);
  }

  public String getDefaultEmail() {
    Glob cloudDesktopUser = repository.find(CloudDesktopUser.KEY);
    if (cloudDesktopUser != null && Strings.isNotEmpty(cloudDesktopUser.get(CloudDesktopUser.EMAIL))) {
      return cloudDesktopUser.get(CloudDesktopUser.EMAIL);
    }
    Glob user = repository.find(User.KEY);
    if (user != null && Strings.isNotEmpty(user.get(User.EMAIL))) {
      return user.get(User.EMAIL);
    }
    return null;
  }
}
