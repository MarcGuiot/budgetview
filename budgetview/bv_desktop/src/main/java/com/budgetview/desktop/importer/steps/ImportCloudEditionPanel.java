package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class ImportCloudEditionPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JLabel progressLabel;
  private JLabel currentEmailAddress;
  private JLabel subscriptionEndDate;
  private GlobRepeat repeat;
  private Action addConnectionAction;
  private Action downloadAction;

  public ImportCloudEditionPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudEditionPanel.splits", repository, localDirectory);

    repeat = builder.addRepeat("connections", CloudProviderConnection.TYPE, GlobMatchers.NONE, GlobComparators.ascending(CloudProviderConnection.BANK_NAME), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, final Glob connection) {
        String bankName = connection.get(CloudProviderConnection.BANK_NAME);
        cellBuilder.add("connectionName", new JLabel(bankName));
        cellBuilder.add("details", getDetailsLabel(connection, bankName));

        JButton editAccountsButton = new JButton(new AbstractAction(Lang.get("import.cloud.edition.editAccounts.button")) {
          public void actionPerformed(ActionEvent e) {
            controller.showCloudAccounts(connection);
          }
        });
        cellBuilder.add("editAccounts", editAccountsButton);
        editAccountsButton.setName("editAccounts:" + bankName);

        JButton updatePasswordButton = new JButton(new UpdatePasswordAction(connection));
        cellBuilder.add("updatePassword", updatePasswordButton);
        updatePasswordButton.setName("updatePassword:" + bankName);

        JButton deleteButton = new JButton(new DeleteConnectionAction(connection));
        cellBuilder.add("delete", deleteButton);
        deleteButton.setName("delete:" + bankName);
      }
    });

    addConnectionAction = new AbstractAction(Lang.get("import.cloud.edition.add.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudBankSelection();
      }
    };
    builder.add("addConnection", addConnectionAction);

    downloadAction = new AbstractAction(Lang.get("import.cloud.edition.download.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudDownload();
      }
    };
    builder.add("download", downloadAction);

    Action modifyEmailAction = new AbstractAction(Lang.get("import.cloud.edition.modifyEmail.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showModifyCloudEmail();
      }
    };
    builder.add("modifyEmailAddress", modifyEmailAction);

    Action unsubscribeAction = new AbstractAction(Lang.get("import.cloud.edition.unsubscribe.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudUnsubscription();
      }
    };
    builder.add("unsubscribe", unsubscribeAction);

    builder.add("close", new AbstractAction(getCloseLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    progressLabel = new JLabel(Lang.get("import.cloud.edition.progress.label"));
    builder.add("progressLabel", progressLabel);
    progressLabel.setVisible(false);

    currentEmailAddress = new JLabel();
    builder.add("currentEmailAddress", currentEmailAddress);

    subscriptionEndDate = new JLabel();
    builder.add("subscriptionEndDate", subscriptionEndDate);

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  private JLabel getDetailsLabel(Glob connection, String bankName) {
    JLabel label = new JLabel();
    if (connection.isTrue(CloudProviderConnection.PASSWORD_ERROR)) {
      label.setText(Lang.get("import.cloud.edition.passwordError"));
    }
    else if (connection.isTrue(CloudProviderConnection.ACTION_NEEDED)) {
      label.setText(Lang.get("import.cloud.edition.actionNeeded"));
    }
    else if (!connection.isTrue(CloudProviderConnection.INITIALIZED)) {
      label.setText(Lang.get("import.cloud.edition.notinitialized"));
    }
    else {
      label.setText(Lang.get("import.cloud.edition.active"));
    }
    label.setName("details:" + bankName);
    return label;
  }

  public void start() {
    repeat.setFilter(GlobMatchers.NONE);
    setAllEnabled(false);
    String email = repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL);
    currentEmailAddress.setText(Lang.get("import.cloud.edition.currentEmailAddress", email));
    subscriptionEndDate.setText("");
    progressLabel.setVisible(true);
    progressPanel.start();
    cloudService.updateBankConnections(repository, new CloudService.Callback() {
      public void processCompletion() {
        Date date = repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.SUBSCRIPTION_END_DATE);
        subscriptionEndDate.setText(Lang.get("import.cloud.edition.subscriptionEndDate", Formatting.toString(date)));
        repeat.setFilter(GlobMatchers.ALL);
        dialog.revalidate();
        setAllEnabled(true);
        progressPanel.stop();
        progressLabel.setVisible(false);
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
        progressPanel.stop();
        progressLabel.setVisible(false);
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
        progressPanel.stop();
        progressLabel.setVisible(false);
      }
    });
  }

  private void setAllEnabled(boolean enabled) {
    addConnectionAction.setEnabled(enabled);
    downloadAction.setEnabled(enabled);
  }

  public void prepareForDisplay() {
    setAllEnabled(true);
  }

  private class DeleteConnectionAction extends AbstractAction {
    private Glob connection;

    public DeleteConnectionAction(Glob connection) {
      super(Lang.get("import.cloud.edition.delete"));
      this.connection = connection;
    }

    public void actionPerformed(ActionEvent e) {
      setEnabled(false);
      progressPanel.start();
      cloudService.deleteBankConnection(connection, repository, new CloudService.Callback() {
        public void processCompletion() {
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

  private class UpdatePasswordAction extends AbstractAction {
    private Glob cloudProviderConnection;

    public UpdatePasswordAction(Glob connection) {
      super(Lang.get("import.cloud.edition.updatePassword"));
      this.cloudProviderConnection = connection;
    }

    public void actionPerformed(ActionEvent e) {
      setAllEnabled(false);
      progressPanel.start();
      GuiUtils.runLater(new Runnable() {
        public void run() {
          cloudService.updateBankFields(cloudProviderConnection.getTargetKey(CloudProviderConnection.BANK), repository, new CloudService.Callback() {
            public void processCompletion() {
              controller.updatePassword(cloudProviderConnection);
              setAllEnabled(true);
              progressPanel.stop();
            }

            public void processSubscriptionError(CloudSubscriptionStatus status) {
              controller.showCloudSubscriptionError(repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
              setAllEnabled(true);
              progressPanel.stop();
            }

            public void processError(Exception e) {
              controller.showCloudError(e);
              setAllEnabled(true);
              progressPanel.stop();
            }
          });
        }
      });
    }
  }
}
