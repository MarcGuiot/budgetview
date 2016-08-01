package com.budgetview.server.persistence.prevayler.users;

import com.budgetview.server.persistence.prevayler.CustomSerializable;
import org.prevayler.Transaction;
import com.budgetview.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.directory.Directory;

import java.util.Date;

public class SetDownloadedVersion implements Transaction, CustomSerializable {
  private long version;
  private static final String TRANSACTION_DV = "DV";

  public SetDownloadedVersion(long version) {
    this.version = version;
  }

  public SetDownloadedVersion() {
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PRootData)prevalentSystem).setDownloadedVersion(version);
  }

  public String getSerializationName() {
    return TRANSACTION_DV;
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
      return TRANSACTION_DV;
    }

    public CustomSerializable create() {
      return new SetDownloadedVersion();
    }
  }

}
