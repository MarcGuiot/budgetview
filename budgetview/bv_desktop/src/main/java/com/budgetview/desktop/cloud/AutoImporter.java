package com.budgetview.desktop.cloud;

import com.budgetview.desktop.cloud.utils.SilentImportDisplay;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.ImportDisplay;
import com.budgetview.desktop.importer.utils.Importer;
import com.budgetview.model.Account;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.RealAccount;
import com.budgetview.model.TransactionImport;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Runnables;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.NotSupported;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class AutoImporter {
  private LocalGlobRepository localRepository;
  private final GlobRepository parentRepository;
  private final Directory directory;
  private final CloudService cloudService;
  private ImportDisplay display;
  private ImportController controller;
  private Callback callback;
  private boolean finished = false;
  private Runnable callbackResult = Runnables.NO_OP;

  public interface Callback {
    void importCompleted(Set<Integer> months, int importedTransactionCount, Set<Key> createdTransactionImports);

    void nothingToImport();

    void needsManualImport();

    void subscriptionError(String email, CloudSubscriptionStatus status);

  }

  public AutoImporter(GlobRepository repository, Directory directory) {
    this.parentRepository = repository;
    this.directory = directory;
    this.cloudService = directory.get(CloudService.class);
    this.localRepository = Importer.loadLocalRepository(parentRepository);
    this.display = new AutoImportDisplay();
    this.controller = new ImportController(display, parentRepository, localRepository, directory);
  }

  public void start(Callback callback) {
    this.callback = callback;
    cloudService.downloadStatement(localRepository, new CloudService.DownloadCallback() {
      public void processCompletion(GlobList importedRealAccounts) {
        if (canBeImportedSilently(importedRealAccounts)) {
          importSilently(importedRealAccounts);
        }
        else {
          AutoImporter.this.callback.needsManualImport();
        }
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(parentRepository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
      }
    });
  }

  private boolean canBeImportedSilently(GlobList importedRealAccounts) {
    for (Glob realAccount : importedRealAccounts) {
      Integer accountId = realAccount.get(RealAccount.ACCOUNT);
      if ((accountId == null) || (!parentRepository.contains(Key.create(Account.TYPE, accountId)))) {
        return false;
      }
    }
    return true;
  }

  private void importSilently(GlobList importedRealAccounts) {
    controller.setReplaceSeries(false);
    controller.importAccounts(importedRealAccounts);
    while (!finished) {
      controller.next();
    }
    callbackResult.run();
  }

  public void dispose() {
    localRepository.rollback();
    localRepository = null;
    controller.dispose();
    controller = null;
  }

  private class AutoImportDisplay extends SilentImportDisplay {

    public void updateForNextImport(String filePath, List<String> dateFormats, Glob importedAccount, Integer accountNumber, Integer accountCount) {
      System.out.println("AutoImportDisplay.updateForNextImport: " + accountNumber);
      finished = false;
    }

    public void showNoImport(Glob remove, boolean first) {
      System.out.println("AutoImportDisplay.showNoImport");
      finished = true;
      callbackResult = new Runnable() {
        public void run() {
          callback.nothingToImport();
        }
      };
    }

    public void showPreview() {
      System.out.println("AutoImportDisplay.showPreview");
    }

    public void showImportCompleted(final Set<Integer> months, final int importedTransactionCount, final int ignoredTransactionCount, final int autocategorizedTransactionCount) {
      System.out.println("AutoImportDisplay.showImportCompleted");
      finished = true;
      final Set<Key> createdTransactionImports = localRepository.getCurrentChanges().getCreated(TransactionImport.TYPE);
      callbackResult = new Runnable() {
        public void run() {
          callback.importCompleted(months, importedTransactionCount, createdTransactionImports);
        }
      };
    }

    public void showCloudSubscriptionError(final String email, final CloudSubscriptionStatus status) {
      System.out.println("AutoImportDisplay.showCloudSubscriptionError");
      finished = true;
      callbackResult = new Runnable() {
        public void run() {
          callback.subscriptionError(email, status);
        }
      };
    }

    public Window getParentWindow() {
      return directory.get(JFrame.class);
    }

    public void closeDialog() {
      System.out.println("AutoImportDisplay.closeDialog");
      throw new NotSupported();
    }
  }
}
