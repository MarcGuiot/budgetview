package com.budgetview.server;

import com.budgetview.server.persistence.direct.InMemoryAccountDataManager;
import com.budgetview.server.persistence.prevayler.InMemoryRootDataManager;
import com.budgetview.server.persistence.prevayler.PrevaylerPersistence;
import com.budgetview.server.persistence.prevayler.users.PRootDataManager;
import com.budgetview.model.PicsouModel;
import com.budgetview.server.persistence.direct.DirectAccountDataManager;
import com.budgetview.server.persistence.dummy.DummyRootDataManager;
import com.budgetview.server.persistence.prevayler.AccountDataManager;
import com.budgetview.server.session.Persistence;
import com.budgetview.server.session.SessionService;
import com.budgetview.server.session.impl.DefaultSessionService;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.io.InputStream;

public class ServerDirectory {
  private Directory serviceDirectory = new DefaultDirectory();

  public ServerDirectory(String prevaylerPath, boolean inMemory) {
    serviceDirectory.add(GlobModel.class, PicsouModel.get());
    AccountDataManager directAccountDataManager;
    if (inMemory) {
      directAccountDataManager = new InMemoryAccountDataManager(null);
    }
    else {
      directAccountDataManager = new DirectAccountDataManager(prevaylerPath, inMemory);
    }
    serviceDirectory.add(Persistence.class,
                         new PrevaylerPersistence(directAccountDataManager,
                                                  inMemory ? new InMemoryRootDataManager() :
                                                  new PRootDataManager(prevaylerPath, serviceDirectory, inMemory),
                                                  serviceDirectory));
    serviceDirectory.add(SessionService.class, new DefaultSessionService(serviceDirectory));
    serviceDirectory.add(ServerRequestProcessingService.class,
                         new DefaultServerRequestProcessingService(serviceDirectory));
  }

  public ServerDirectory(InputStream inputStream) {
    serviceDirectory.add(GlobModel.class, PicsouModel.get());
    serviceDirectory.add(Persistence.class,
                         new PrevaylerPersistence(new InMemoryAccountDataManager(inputStream),
                                                  new DummyRootDataManager(),
                                                  serviceDirectory));
    serviceDirectory.add(SessionService.class, new DefaultSessionService(serviceDirectory));
    serviceDirectory.add(ServerRequestProcessingService.class,
                         new DefaultServerRequestProcessingService(serviceDirectory));
  }

  public void close() {
    serviceDirectory.get(Persistence.class).close();
  }

  public Directory getServiceDirectory() {
    return serviceDirectory;
  }
}
