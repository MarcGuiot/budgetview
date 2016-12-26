package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;

public class ImportCloudValidationPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private Action nextAction;
  private JTextField codeField;
  private JLabel errorLabel;
  private String email = "...";
  private JEditorPane message;
  private Action backAction;

  public ImportCloudValidationPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudValidationPanel.splits", repository, localDirectory);

    nextAction = new AbstractAction(Lang.get("import.cloud.validation.next")) {
      public void actionPerformed(ActionEvent e) {
        processNext();
      }
    };

    message = GuiUtils.createReadOnlyHtmlComponent(Lang.get("import.cloud.validation.message", "..."));
    updateMessage();
    builder.add("message", message);

    codeField = new JTextField();
    builder.add("codeField", codeField);
    codeField.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        processNext();
      }
    });
    codeField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        nextAction.setEnabled(Strings.isNotEmpty(codeField.getText()));
      }
    });

    errorLabel = new JLabel(" ");
    builder.add("error", errorLabel);

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    backAction = new AbstractAction(Lang.get("import.cloud.validation.back")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudSignup();
      }
    };
    backAction.setEnabled(false);
    builder.add("back", new JButton(backAction));

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  public void setEmail(String email) {
    this.email = email;
    updateMessage();
  }

  public void updateMessage() {
    if (message != null) {
      message.setText(Lang.get("import.cloud.validation.message", email));
    }
  }

  private void processNext() {

    errorLabel.setText(" ");
    backAction.setEnabled(false);

    final String code = Strings.trim(codeField.getText());
    if (Strings.isNullOrEmpty(code)) {
      errorLabel.setText(Lang.get("import.cloud.validation.nocode"));
      return;
    }

    setAllEnabled(false);
    progressPanel.start();
    cloudService.validate(email, codeField.getText(), repository, new CloudService.ValidationCallback() {
      public void processCompletionAndSelectBank() {
        System.out.println("ImportCloudValidationPanel.processCompletionAndSelectBank");
        controller.showCloudBankSelection();
        progressPanel.stop();
        setAllEnabled(true);
        backAction.setEnabled(false);
      }

      public void processCompletionAndDownload() {
        System.out.println("ImportCloudValidationPanel.processCompletionAndDownload");
        controller.showCloudDownload();
        progressPanel.stop();
        setAllEnabled(true);
        backAction.setEnabled(false);
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        System.out.println("ImportCloudValidationPanel.processSubscriptionError");
        controller.showCloudSubscriptionError(email, status);
        progressPanel.stop();
        setAllEnabled(true);
        backAction.setEnabled(false);
      }

      public void processTempTokenExpired() {
        System.out.println("ImportCloudValidationPanel.processTempTokenExpired");
        errorLabel.setText(Lang.get("import.cloud.validation.tempcode.expired"));
        setAllEnabled(true);
        progressPanel.stop();
        backAction.putValue(Action.NAME, Lang.get("import.cloud.validation.back"));
        backAction.setEnabled(true);
        nextAction.setEnabled(false);
      }

      public void processInvalidCode() {
        System.out.println("ImportCloudValidationPanel.processInvalidCode: " + Lang.get("import.cloud.validation.invalid.code"));
        errorLabel.setText(Lang.get("import.cloud.validation.invalid.code"));
        setAllEnabled(true);
        progressPanel.stop();
        backAction.putValue(Action.NAME, Lang.get("import.cloud.validation.resend"));
        backAction.setEnabled(true);
        nextAction.setEnabled(false);
      }

      public void processError(Exception e) {
        System.out.println("ImportCloudValidationPanel.processError");
        controller.showCloudError();
        progressPanel.stop();
        setAllEnabled(true);
        backAction.setEnabled(false);
      }
    });
  }

  private void setAllEnabled(boolean enabled) {
    System.out.println("ImportCloudValidationPanel.setAllEnabled: " + enabled);
    codeField.setEnabled(enabled);
    nextAction.setEnabled(enabled);
  }

  public void prepareForDisplay() {
    codeField.setText("");
    errorLabel.setText("");
    setAllEnabled(true);
    backAction.setEnabled(false);
    codeField.requestFocus();
  }
}
