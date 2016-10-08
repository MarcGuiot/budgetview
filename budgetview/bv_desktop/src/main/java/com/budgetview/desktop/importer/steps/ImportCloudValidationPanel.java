package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudValidationPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private Action nextAction;
  private JTextField codeField;
  private JLabel errorLabel;
  private String email;

  public ImportCloudValidationPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudValidationPanel.splits", repository, localDirectory);

    nextAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        processNext();
      }
    };

    codeField = new JTextField();
    builder.add("code", codeField);

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

  public void setEmail(String email) {
    this.email = email;
  }

  private void processNext() {
    progressPanel.start();
    cloudService.validate(email, codeField.getText(), repository, new CloudService.ValidationCallback() {

      public void processCompletion() {
        System.out.println("ImportCloudValidationPanel.processCompletion: validate OK");
        controller.showCloudBankSelection();
        progressPanel.stop();
      }

      public void processInvalidCode() {
        System.out.println("ImportCloudValidationPanel.processCompletion: invalid code");
        errorLabel.setText(Lang.get("import.cloud.validation.invalid.code"));
        errorLabel.setVisible(true);
      }

      public void processError(Exception e) {
        System.out.println("ImportCloudValidationPanel.processCompletion: error");
        controller.showCloudError();
        progressPanel.stop();
      }
    });
  }

  public void requestFocus() {
  }
}
