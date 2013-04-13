package org.designup.picsou.gui.license;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

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
    if (!ConfigService.waitEndOfConfigRequest(directory, -1)){
      return ;
    }
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        synchronized public void run() {
          if (repository != null && directory != null) {
            repository.startChangeSet();
            try {
              ConfigService.check(directory, repository);
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
