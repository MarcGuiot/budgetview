package com.budgetview.desktop;

import com.budgetview.client.DataAccess;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.client.http.EncryptToTransportDataAccess;
import com.budgetview.client.local.LocalSessionDataTransport;
import com.budgetview.desktop.startup.AppPaths;
import com.budgetview.session.SessionDirectory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AppCore {

  public static class AppCoreBuilder {
    private final String user;
    private final String password;
    private final String snapshot;
    private Directory directory;

    private AppCoreBuilder(String user, String password, String snapshot) throws IOException {
      this.user = user;
      this.password = password;
      this.snapshot = snapshot;
      Application.clearRepositoryIfNeeded();
      Application.changeDate();
      Log.setDebugEnabled(true);
      directory = Application.createDirectory();
    }

    public <T> AppCoreBuilder set(Class<T> clazz, T object) {
      directory.replace(clazz, object);
      return this;
    }

    public AppCore complete() throws FileNotFoundException {
      SessionDirectory sessionDirectory = createServerDirectory(snapshot);
      directory.add(SessionDirectory.class, sessionDirectory);
      DataAccess dataAccess =
        new EncryptToTransportDataAccess(new LocalSessionDataTransport(sessionDirectory.getServiceDirectory()),
                                         directory);
      dataAccess.connect(Application.JAR_VERSION);

      boolean registered = isRegistered(user, password, dataAccess);
      PicsouInit init = PicsouInit.init(dataAccess, directory, registered, false);
      init.loadUserData(user, false, false).load();
      return new AppCore(init);
    }
  }

  public static AppCoreBuilder init(String user, String password, String snapshot) throws IOException {
    return new AppCoreBuilder(user, password, snapshot);
  }

  private final PicsouInit init;

  private AppCore(PicsouInit init) {
    this.init = init;
  }

  public GlobRepository getRepository() {
    return init.getRepository();
  }

  public Directory getDirectory() {
    return init.getDirectory();
  }

  private static SessionDirectory createServerDirectory(String snapshot) throws FileNotFoundException {
    SessionDirectory sessionDirectory;
    if (snapshot != null) {
      sessionDirectory = new SessionDirectory(new FileInputStream(snapshot));
    }
    else {
      sessionDirectory = new SessionDirectory(AppPaths.getCurrentDataPath(), Application.isDataInMemory());
    }
    return sessionDirectory;
  }

  private static boolean isRegistered(String user, String password, DataAccess dataAccess) {
    boolean registered = false;
    try {
      registered = dataAccess.createUser(user, password.toCharArray(), false);
    }
    catch (UserAlreadyExists e) {
      registered = dataAccess.initConnection(user, password.toCharArray(), false);
    }
    return registered;
  }
}
