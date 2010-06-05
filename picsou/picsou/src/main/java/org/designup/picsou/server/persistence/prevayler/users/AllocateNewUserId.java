package org.designup.picsou.server.persistence.prevayler.users;

import org.prevayler.TransactionWithQuery;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.Date;

public class AllocateNewUserId implements TransactionWithQuery, CustomSerializable {
  private String name;
  private static final String TRANSACTION_NAME = "AllocateNewUserId";


  public AllocateNewUserId(String name) {
    this.name = name;
  }

  public AllocateNewUserId() {
  }

  public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {
    PRootData rootData = ((PRootData)prevalentSystem);
    return rootData.getNewUserId(executionTime, name);
  }

  public String getSerializationName() {
    return TRANSACTION_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    switch (version) {
      case 1:
        readV1(input);
        break;
      default:
        throw new UnexpectedApplicationState("version " + version + " not managed");
    }
  }

  private void readV1(SerializedInput input) {
    name = input.readJavaString();
   }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(1);
    output.writeUtf8String(name);
  }
  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new AllocateNewUserId();
    }
  }
}
