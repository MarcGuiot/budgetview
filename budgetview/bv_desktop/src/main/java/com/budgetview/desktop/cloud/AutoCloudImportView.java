package com.budgetview.desktop.cloud;

import com.budgetview.desktop.View;
import com.budgetview.desktop.WindowManager;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.Transaction;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Runnables;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AutoCloudImportView extends View {

  private JPanel panel;
  private JLabel label;
  private ProgressPanel progressPanel;
  private JButton actionButton;
  private JButton cancelButton;
  private Runnable switchToMainPanel = Runnables.NO_OP;

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
    return repository.contains(CloudDesktopUser.KEY) && repository.get(CloudDesktopUser.KEY).isTrue(CloudDesktopUser.REGISTERED);
  }

  public AbstractAction gotoCategorization(final Set<Key> createdTransactionImports, final Runnable switchToMainPanel) {
    return new AbstractAction(Lang.get("autoimport.cloud.goto.categorization")) {
      public void actionPerformed(ActionEvent e) {
        System.out.println("AutoCloudImportView.actionPerformed: " + createdTransactionImports);
        GlobList transactions = repository.getAll(Transaction.TYPE, GlobMatchers.linkedTo(createdTransactionImports, Transaction.IMPORT));
        directory.get(NavigationService.class).gotoCategorization(transactions, false);
        switchToMainPanel.run();
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

      public void importCompleted(Set<Integer> months, int importedTransactionCount, Set<Key> createdTransactionImports) {
        String message = importedTransactionCount == 1 ?
          Lang.get("autoimport.cloud.import.completed.one") :
          Lang.get("autoimport.cloud.import.completed.many", importedTransactionCount);
        update(message, gotoCategorization(createdTransactionImports, switchToMainPanel));
      }

      public void nothingToImport() {

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
