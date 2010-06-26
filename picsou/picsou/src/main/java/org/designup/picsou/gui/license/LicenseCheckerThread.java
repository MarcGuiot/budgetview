package org.designup.picsou.gui.license;

import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobRepository;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;

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
    ConfigService.waitEndOfConfigRequest(directory);
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          repository.startChangeSet();
          try {
            ConfigService.check(directory, repository);
            repository.update(User.KEY, User.CONNECTED, true);
          }
          finally {
            repository.completeChangeSet();
          }
        }
      });
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
