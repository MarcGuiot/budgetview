package org.designup.picsou.server;

import org.designup.picsou.model.PicsouModel;
import org.designup.picsou.server.persistence.direct.DirectAccountDataManagerWithSnapshot;
import org.designup.picsou.server.persistence.prevayler.PrevaylerPersistence;
import org.designup.picsou.server.persistence.prevayler.users.PRootDataManager;
import org.designup.picsou.server.session.Persistence;
import org.designup.picsou.server.session.SessionService;
import org.designup.picsou.server.session.impl.DefaultSessionService;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public class ServerDirectory {
  private Directory serviceDirectory = new DefaultDirectory();

  public ServerDirectory(String prevaylerPath, boolean inMemory) {
    serviceDirectory.add(GlobModel.class, PicsouModel.get());
    serviceDirectory.add(Persistence.class,
                         new PrevaylerPersistence(new DirectAccountDataManagerWithSnapshot(prevaylerPath, inMemory),
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
