package com.budgetview.session;

import com.budgetview.model.PicsouModel;
import com.budgetview.persistence.direct.DirectAccountDataManager;
import com.budgetview.persistence.direct.InMemoryAccountDataManager;
import com.budgetview.persistence.dummy.DummyRootDataManager;
import com.budgetview.persistence.prevayler.AccountDataManager;
import com.budgetview.persistence.prevayler.InMemoryRootDataManager;
import com.budgetview.persistence.prevayler.PrevaylerPersistence;
import com.budgetview.persistence.prevayler.users.PRootDataManager;
import com.budgetview.session.states.Persistence;
import com.budgetview.session.states.SessionStateHandler;
import com.budgetview.session.states.impl.DefaultSessionStateHandler;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.io.InputStream;

public class SessionDirectory {
  private Directory serviceDirectory = new DefaultDirectory();

  public SessionDirectory(String prevaylerPath, boolean inMemory) {
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
    serviceDirectory.add(SessionService.class,
                         new DefaultSessionService(new DefaultSessionStateHandler(serviceDirectory)));
  }

  public SessionDirectory(InputStream inputStream) {
    serviceDirectory.add(GlobModel.class, PicsouModel.get());
    serviceDirectory.add(Persistence.class,
                         new PrevaylerPersistence(new InMemoryAccountDataManager(inputStream),
                                                  new DummyRootDataManager(),
                                                  serviceDirectory));
    serviceDirectory.add(SessionStateHandler.class, new DefaultSessionStateHandler(serviceDirectory));
    serviceDirectory.add(SessionService.class,
                         new DefaultSessionService(new DefaultSessionStateHandler(serviceDirectory)));
  }

  public void close() {
    serviceDirectory.get(Persistence.class).close();
  }

  public Directory getServiceDirectory() {
    return serviceDirectory;
  }
}
