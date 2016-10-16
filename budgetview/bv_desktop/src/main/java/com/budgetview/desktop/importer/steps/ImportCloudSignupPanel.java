package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ImportCloudSignupPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private Action nextAction;
  private Glob currentConnection;
  private JTextField emailField;
  private JLabel errorLabel;

  public ImportCloudSignupPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
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

    emailField = new JTextField();
    builder.add("email", emailField);
    emailField.setAction(nextAction);

    errorLabel = new JLabel(" ");
    builder.add("error", errorLabel);
    errorLabel.setVisible(false);

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(textForCloseButton) {
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
    if (!looksLikeAnEmail(email)) {
      errorLabel.setText(Lang.get("import.cloud.signup.missing.email"));
      errorLabel.setVisible(true);
      return;
    }

    setAllEnabled(false);
    progressPanel.start();
    cloudService.signup(email, repository, new CloudService.Callback() {
      public void processCompletion() {
        controller.showCloudValidation(email);
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError();
        progressPanel.stop();
      }
    });
  }

  private void setAllEnabled(boolean enabled) {
    nextAction.setEnabled(enabled);
    emailField.setEnabled(enabled);
  }

  private boolean looksLikeAnEmail(String email) {
    return (email.length() > 3) && email.contains("@");
  }

  public void prepareForDisplay() {
    setAllEnabled(true);
    emailField.requestFocus();
  }
}
