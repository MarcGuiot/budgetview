package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudEditionPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private GlobRepeat repeat;
  private AbstractAction addConnectionAction;
  private Action backAction;
  private JTextField emailField;
  private JLabel errorLabel;

  public ImportCloudEditionPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudEditionPanel.splits", repository, localDirectory);

    backAction = new AbstractAction(Lang.get("import.cloud.edition.back")) {
      public void actionPerformed(ActionEvent e) {
        processNext();
      }
    };


    repeat = builder.addRepeat("connections", CloudProviderConnection.TYPE, GlobMatchers.NONE, GlobComparators.ascending(CloudProviderConnection.NAME), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, Glob connection) {
        cellBuilder.add("name", new JLabel(connection.get(CloudProviderConnection.NAME)));
        cellBuilder.add("delete", new DeleteConnectionAction(connection.get(CloudProviderConnection.PROVIDER_ID)));
      }
    });

    addConnectionAction = new AbstractAction(Lang.get("import.cloud.edition.add.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudBankSelection();
      }
    };
    builder.add("back", backAction);

    errorLabel = new JLabel(" ");
    builder.add("errorMessage", errorLabel);
    errorLabel.setVisible(false);

    builder.add("next", backAction);
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

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(email, status);
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError();
        progressPanel.stop();
      }
    });
  }

  private void setAllEnabled(boolean enabled) {
    repeat.setFilter(enabled ? GlobMatchers.ALL : GlobMatchers.NONE);
    backAction.setEnabled(enabled);
    emailField.setEnabled(enabled);
  }

  private boolean looksLikeAnEmail(String email) {
    return (email.length() > 3) && email.contains("@");
  }

  public void prepareForDisplay() {
    setAllEnabled(true);
    emailField.requestFocus();
  }

  private class DeleteConnectionAction extends AbstractAction {
    public DeleteConnectionAction(Integer connectionId) {
      super(Lang.get("import.cloud.edition.delete"));
    }

    public void actionPerformed(ActionEvent e) {

    }
  }
}
