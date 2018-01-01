package com.budgetview.desktop.cloud;

import com.budgetview.desktop.View;
import com.budgetview.desktop.WindowManager;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AutoCloudImportView extends View {

  private JPanel panel;
  private JLabel label;
  private ProgressPanel progressPanel;
  private JButton actionButton;
  private JButton cancelButton;
  private Runnable switchToMainPanel = new Runnable() {
    public void run() {
    }
  };

  public AutoCloudImportView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/cloud/autoCloudImportView.splits",
                                                      repository, directory);

    label = new JLabel(Lang.get("autoimport.cloud.download.message"));
    builder.add("label", label);

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    cancelButton = new JButton();
    builder.add("cancel", cancelButton);

    actionButton = new JButton();
    builder.add("action", actionButton);
    actionButton.setVisible(false);

    panel = builder.load();
  }

  private boolean shouldDisplay() {
    return false; // repository.contains(CloudDesktopUser.KEY) && repository.get(CloudDesktopUser.KEY).isTrue(CloudDesktopUser.REGISTERED);
  }

  public AbstractAction gotoCategorization() {
    return new AbstractAction(Lang.get("autoimport.cloud.goto.categorization")) {
      public void actionPerformed(ActionEvent e) {
        directory.get(NavigationService.class).gotoCategorizationForLastImport();
      }
    };
  }

  public void displayIfNeeded(final WindowManager windowManager, final JPanel mainPanel) {

    switchToMainPanel = new Runnable() {
      public void run() {
        windowManager.setPanel(mainPanel);
      }
    };

    if (!shouldDisplay()) {
      switchToMainPanel.run();
      return;
    }

    final AutoImporter autoImporter = new AutoImporter(repository, directory);

    cancelButton.setAction(new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        switchToMainPanel.run();
        directory.get(NavigationService.class).gotoDashboard();
        autoImporter.dispose();
      }
    });

    progressPanel.start();
    windowManager.setPanel(panel);
    autoImporter.start(new AutoImporter.Callback() {
      public void importCompleted() {
        update(Lang.get(""), gotoCategorization());
      }

      public void needsManualImport() {

      }

      public void subscriptionError(String email, CloudSubscriptionStatus status) {

      }
    });
  }

  private void update(String message, AbstractAction action) {
    actionButton.setAction(action);
    actionButton.setVisible(true);
  }
}
