package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.util.Date;

class Register implements Transaction, CustomSerializable {
  private static final byte V1 = 1;
  private static final String REGISTER = "register";
  private byte[] mail;
  private byte[] signature;

  private Register() {
  }

  public Register(byte[] mail, byte[] signature) {
    this.mail = mail;
    this.signature = signature;
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PRootData)prevalentSystem).register(mail, signature);
  }

  public String getSerializationName() {
    return REGISTER;
  }

  public void read(SerializedInput input, Directory directory) {
    int version = input.readNotNullInt();
    if (version == V1) {
      mail = input.readBytes();
      signature = input.readBytes();
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.write(V1);
    output.writeBytes(mail);
    output.writeBytes(signature);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return REGISTER;
    }

    public CustomSerializable create() {
      return new Register();
    }
  }
}
