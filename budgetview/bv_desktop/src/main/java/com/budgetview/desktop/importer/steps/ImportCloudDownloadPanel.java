package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudDownloadPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;

  public ImportCloudDownloadPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudDownloadPanel.splits", repository, localDirectory);

    Action nextAction = new AbstractAction(getNextLabel()) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    nextAction.setEnabled(false);

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(getCancelLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  public void prepareForDisplay() {
  }

  public void start(boolean firstDownload) {
    System.out.println("ImportCloudDownloadPanel.start(" + firstDownload + ")");
    progressPanel.start();
    CloudService.DownloadCallback callback = new CloudService.DownloadCallback() {
      public void processCompletion(GlobList importedRealAccounts) {
        System.out.println("ImportCloudDownloadPanel.processCompletion for " + importedRealAccounts);
        controller.setReplaceSeries(false);
        controller.importAccounts(importedRealAccounts);
        progressPanel.stop();
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
        progressPanel.stop();
      }

      public void processTimeout() {
        controller.showCloudTimeout();
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
        progressPanel.stop();
      }
    };
    if (firstDownload) {
      cloudService.downloadInitialStatement(repository, callback);
    }
    else {
      cloudService.downloadStatement(repository, callback);
    }
  }
}
