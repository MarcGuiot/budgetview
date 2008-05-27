package org.designup.picsou.server.persistence.prevayler.accounts;

import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.prevayler.Query;

import java.util.Date;

class GetSerializedUserDataTransaction implements Query {
  private SerializedOutput output;

  public GetSerializedUserDataTransaction(SerializedOutput output) {
    this.output = output;
  }

  public Object query(Object prevalentSystem, Date executionTime) throws Exception {
    ((UserData) prevalentSystem).getUserData(output);
    return output;
  }
}
