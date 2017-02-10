package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.ConfirmationDialog;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudUnsubscriptionPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JEditorPane message = GuiUtils.createReadOnlyHtmlComponent();
  private JButton unsubscribeButton;

  public ImportCloudUnsubscriptionPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudUnsubscriptionPanel.splits", repository, localDirectory);

    unsubscribeButton = new JButton(new AbstractAction(Lang.get("import.cloud.unsubscription.action")) {
      public void actionPerformed(ActionEvent e) {
        unsubscribe();
      }
    });
    builder.add("unsubscribe", unsubscribeButton);

    builder.add("close", new AbstractAction(getCloseLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    builder.add("message", message);

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  public void prepareForDisplay() {
    message.setText(Lang.get("import.cloud.unsubscription.message"));
    GuiUtils.scrollToTop(message);
    unsubscribeButton.setEnabled(true);
    unsubscribeButton.setVisible(true);
  }

  private void unsubscribe() {
    ConfirmationDialog confirmation = new ConfirmationDialog("import.cloud.unsubscription.confirm.title",
                                                             Lang.get("import.cloud.unsubscription.confirm.message"),
                                                             dialog, localDirectory) {
      protected void processOk() {
        doUnsubscribe();
      }

      protected void processCancel() {
      }
    };
    confirmation.show();
  }

  private void doUnsubscribe() {
    unsubscribeButton.setEnabled(false);
    progressPanel.start();
    cloudService.deleteCloudAccount(repository, new CloudService.UnsubscriptionCallback() {
      public void processCompletion() {
        message.setText(Lang.get("import.cloud.unsubscription.done"));
        controller.saveCloudUnsubscription();
        GuiUtils.scrollToTop(message);
        unsubscribeButton.setVisible(false);
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
        progressPanel.stop();
      }
    });
  }
}
