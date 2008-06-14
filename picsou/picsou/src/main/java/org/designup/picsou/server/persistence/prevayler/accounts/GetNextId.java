package org.designup.picsou.server.persistence.prevayler.accounts;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.SureTransactionWithQuery;

import java.util.Date;

public class GetNextId implements SureTransactionWithQuery, CustomSerializable {
  private static final byte V1 = 1;
  private String globTypeName;
  private Integer count;
  private static final String GET_NEXT_ACCOUNT_ID_NAME = "GetNextAccountId";

  public GetNextId(String globTypeName, Integer count) {
    this.globTypeName = globTypeName;
    this.count = count;
  }

  public GetNextId() {
  }

  public Object executeAndQuery(Object prevalentSystem, Date executionTime) {
    return ((UserData)prevalentSystem).getNextId(globTypeName, count);
  }

  public String getSerializationName() {
    return GET_NEXT_ACCOUNT_ID_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    if (version != V1) {
      throw new InvalidData("Version " + version + " not managed");
    }
    count = input.readInteger();
    globTypeName = input.readString();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V1);
    output.writeInteger(count);
    output.writeString(globTypeName);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return GET_NEXT_ACCOUNT_ID_NAME;
    }

    public CustomSerializable create() {
      return new GetNextId();
    }
  }


}
