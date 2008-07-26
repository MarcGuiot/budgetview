package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.TransactionWithQuery;

import java.util.Date;

class GetAndUpdateCount implements TransactionWithQuery, CustomSerializable {

  private static final String TRANSACTION_NAME = "getAndUpdateCount";
  private static final int V1 = 1;

  GetAndUpdateCount() {
  }

  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    PRootData rootData = (PRootData)prevalentSystem;
    return rootData.getRepoInfo();
  }

  public String getSerializationName() {
    return TRANSACTION_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    int version = input.readNotNullInt();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.write(V1);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new GetAndUpdateCount();
    }
  }

}
