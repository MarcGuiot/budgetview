package com.budgetview.desktop.license.activation;

import com.budgetview.desktop.userconfig.UserConfigService;
import com.budgetview.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class LicenseCheckerThread extends Thread {
  private Directory directory;
  private GlobRepository repository;

  public static LicenseCheckerThread launch(Directory directory, GlobRepository repository) {
    LicenseCheckerThread thread = new LicenseCheckerThread(directory, repository);
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  public LicenseCheckerThread(Directory directory, GlobRepository repository) {
    this.directory = directory;
    this.repository = repository;
  }

  public void run() {
    if (!UserConfigService.waitEndOfConfigRequest(directory, -1)){
      return ;
    }
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        synchronized public void run() {
          if (repository != null && directory != null) {
            repository.startChangeSet();
            try {
              UserConfigService.check(directory, repository);
              repository.update(User.KEY, User.CONNECTED, true);
            }
            finally {
              repository.completeChangeSet();
            }
          }
        }
      });
    }
    catch (Exception e) {
    }
  }

  synchronized public void shutdown() {
    repository = null;
    directory = null;
  }
}
