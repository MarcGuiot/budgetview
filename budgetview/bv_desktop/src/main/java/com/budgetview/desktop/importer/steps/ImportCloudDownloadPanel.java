package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudDownloadPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;

  public ImportCloudDownloadPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudErrorPanel.splits", repository, localDirectory);

    Action nextAction = new AbstractAction("import.cloud.validation.next") {
      public void actionPerformed(ActionEvent e) {
      }
    };
    nextAction.setEnabled(false);

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

  public void prepareForDisplay() {
  }

  public void start() {
    progressPanel.start();
    cloudService.downloadStatement(repository, new CloudService.DownloadCallback() {
      public void processCompletion(GlobList importedRealAccounts) {
        controller.setReplaceSeries(false);
        controller.importAccounts(importedRealAccounts);
        progressPanel.stop();
      }

      public void processTimeout() {
        controller.showCloudError();
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError();
        progressPanel.stop();
      }
    });
  }
}
