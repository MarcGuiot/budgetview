package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudFirstDownloadPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JLabel noDataLabel;
  private int connectionId;

  public ImportCloudFirstDownloadPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudFirstDownloadPanel.splits", repository, localDirectory);

    builder.add("close", new AbstractAction(Lang.get("import.cloud.first.download.button")) {
      public void actionPerformed(ActionEvent e) {
        startCheck();
      }
    });

    noDataLabel = new JLabel(Lang.get("import.cloud.first.download.nodata"));
    builder.add("noData", noDataLabel);

    builder.add("download", new AbstractAction(getCancelLabel()) {
      public void actionPerformed(ActionEvent e) {
        startCheck();
      }
    });

    builder.add("next", new DisabledAction(getNextLabel()));
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

  public void setConnectionId(int connectionId) {
    this.connectionId = connectionId;
  }

  public void prepareForDisplay() {
    noDataLabel.setVisible(false);
  }

  public void startCheck() {
    noDataLabel.setVisible(false);
    progressPanel.start();
    cloudService.checkBankConnectionReady(connectionId, repository, new CloudService.BankConnectionCheckCallback() {
      public void processCompletion(boolean ready) {
        if (ready) {
          System.out.println("ImportCloudFirstDownloadPanel.processCompletion: statements ready, starting download");
          controller.showCloudDownload();
        }
        else {
          System.out.println("ImportCloudFirstDownloadPanel.processCompletion: statements not ready");
          noDataLabel.setVisible(true);
        }
        progressPanel.stop();
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
        progressPanel.stop();
      }
    });
  }
}
