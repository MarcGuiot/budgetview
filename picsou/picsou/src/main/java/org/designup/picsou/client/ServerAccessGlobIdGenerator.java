package org.designup.picsou.client;

import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;

public class ServerAccessGlobIdGenerator implements GlobIdGenerator {
  private final ServerAccess serverAccess;

  public ServerAccessGlobIdGenerator(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public int getNextId(IntegerField keyField, int idCount) {
    return serverAccess.getNextId(keyField.getGlobType().getName(), idCount);
  }
}
