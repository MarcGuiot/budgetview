package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.model.Glob;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudFirstDownloadPanel extends AbstractImportStepPanel {

  private final LocalGlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JLabel noDataLabel;
  private Glob providerConnection;
  private Timer timer;

  public ImportCloudFirstDownloadPanel(PicsouDialog dialog, ImportController controller, LocalGlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudFirstDownloadPanel.splits", repository, localDirectory);

    builder.add("download", new AbstractAction(Lang.get("import.cloud.first.download.button")) {
      public void actionPerformed(ActionEvent e) {
        startCheck();
      }
    });

    noDataLabel = new JLabel(Lang.get("import.cloud.first.download.nodata"));
    builder.add("noData", noDataLabel);

    timer = new Timer(3000, new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        noDataLabel.setVisible(false);
        timer.stop();
      }
    });

    builder.add("next", new DisabledAction(getNextLabel()));
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

  public void setConnection(Glob providerConnection) {
    this.providerConnection = providerConnection;
  }

  public void prepareForDisplay() {
    noDataLabel.setVisible(false);
  }

  public void startCheck() {
    noDataLabel.setVisible(false);
    progressPanel.start();
    if (providerConnection == null) {
      throw new InvalidState("providerConnection is null");
    }
    cloudService.checkBankConnectionReady(providerConnection, repository, new CloudService.BankConnectionCheckCallback() {
      public void processCompletion(boolean ready) {
        if (ready) {
          controller.showCloudDownload();
        }
        else {
          noDataLabel.setVisible(true);
          if (!timer.isRunning()) {
            timer.start();
          }
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

  public void dispose() {
    if (timer != null && timer.isRunning()) {
      timer.stop();
    }
    super.dispose();
  }
}
