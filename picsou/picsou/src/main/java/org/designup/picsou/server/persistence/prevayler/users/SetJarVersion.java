package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.util.Date;

public class SetJarVersion implements Transaction, CustomSerializable {
  private long version;
  private static final String TRANSACTION_JAR_DV = "JAR_DV";

  public SetJarVersion(long version) {
    this.version = version;
  }

  public SetJarVersion() {
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PRootData)prevalentSystem).setJarVersion(version);
  }

  public String getSerializationName() {
    return TRANSACTION_JAR_DV;
  }

  public void read(SerializedInput input, Directory directory) {
    int version = input.readNotNullInt();
    if (version == 1){
      this.version = input.readNotNullLong();
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.write(1);
    output.write(version);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_JAR_DV;
    }

    public CustomSerializable create() {
      return new SetJarVersion();
    }
  }

}
