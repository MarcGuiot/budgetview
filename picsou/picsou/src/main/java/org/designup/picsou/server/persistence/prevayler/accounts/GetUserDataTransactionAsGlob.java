package org.designup.picsou.server.persistence.prevayler.accounts;

import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Query;

import java.util.Date;

class GetUserDataTransactionAsGlob implements Query {
  private SerializedOutput output;

  public GetUserDataTransactionAsGlob(SerializedOutput output) {
    this.output = output;
  }

  public Object query(Object prevalentSystem, Date executionTime) throws Exception {
    ((UserData)prevalentSystem).getUserData(output);
    return output;
  }
}
