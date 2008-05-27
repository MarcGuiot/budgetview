package org.designup.picsou.server;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.server.model.ServerModel;
import org.designup.picsou.server.persistence.prevayler.PrevaylerPersistence;
import org.designup.picsou.server.persistence.prevayler.accounts.PAccountDataManager;
import org.designup.picsou.server.persistence.prevayler.users.PRootDataManager;
import org.designup.picsou.server.session.Persistence;
import org.designup.picsou.server.session.SessionService;
import org.designup.picsou.server.session.impl.DefaultSessionService;

public class ServerDirectory {
  private Directory serviceDirectory = new DefaultDirectory();

  public ServerDirectory(String prevaylerPath, boolean inMemory) {
    serviceDirectory.add(GlobModel.class, ServerModel.get());
    serviceDirectory.add(Persistence.class,
                         new PrevaylerPersistence(new PAccountDataManager(prevaylerPath, serviceDirectory, inMemory),
                                                  new PRootDataManager(prevaylerPath, serviceDirectory, inMemory),
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

  static public Directory getNewDirectory(String prevaylerUrlPath) {
    return new ServerDirectory(prevaylerUrlPath, false).serviceDirectory;
  }
}
