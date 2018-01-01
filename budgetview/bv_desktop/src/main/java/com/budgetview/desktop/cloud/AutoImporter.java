package com.budgetview.desktop.cloud;

import com.budgetview.desktop.cloud.utils.SilentImportDisplay;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.ImportDisplay;
import com.budgetview.desktop.importer.utils.Importer;
import com.budgetview.model.Account;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.NotSupported;

import javax.swing.*;
import java.awt.*;

public class AutoImporter {
  private LocalGlobRepository localRepository;
  private final GlobRepository parentRepository;
  private final Directory directory;
  private final CloudService cloudService;
  private ImportDisplay display;
  private ImportController controller;
  private Callback callback;

  public interface Callback {
    void importCompleted();

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
  }

  public void dispose() {
    localRepository.rollback();
    localRepository = null;
    controller.dispose();
    controller = null;
  }

  private class AutoImportDisplay extends SilentImportDisplay {
    public void showPreview() {
    }

    public void showCloudSubscriptionError(String email, CloudSubscriptionStatus status) {
      callback.subscriptionError(email, status);
    }

    public Window getParentWindow() {
      return directory.get(JFrame.class);
    }

    public void closeDialog() {
      throw new NotSupported();
    }
  }
}
