package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

public class InitialRepoIdTransaction implements Transaction, CustomSerializable {
  private static final int V1 = 1;
  private static String TRANSACTION_NAME = "InitialRepoId";
  private byte[] repoId;

  public InitialRepoIdTransaction() {
  }

  private InitialRepoIdTransaction(byte[] repoId) {
    this.repoId = repoId;
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PRootData)prevalentSystem).setRepoId(repoId);
  }

  public String getSerializationName() {
    return TRANSACTION_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    int version = input.readNotNullInt();
    repoId = input.readBytes();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.write(V1);
    output.writeBytes(repoId);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  public static Transaction create() {
    Random random = new SecureRandom();
    Utils.beginRemove();
    random = new Random();
    Utils.endRemove();
    byte repoId[] = new byte[50];
    random.nextBytes(repoId);
    return new InitialRepoIdTransaction(repoId);
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new InitialRepoIdTransaction();
    }
  }
}
