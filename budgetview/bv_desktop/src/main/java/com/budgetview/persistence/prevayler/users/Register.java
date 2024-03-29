package com.budgetview.persistence.prevayler.users;

import com.budgetview.persistence.prevayler.CustomSerializable;
import com.budgetview.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.util.Date;

public class Register implements Transaction, CustomSerializable {
  private static final byte V1 = 1;
  private static final byte V2 = 2;
  private static final String REGISTER = "register";
  private byte[] mail;
  private byte[] signature;
  private String activationCode;

  private Register() {
  }

  public Register(byte[] mail, byte[] signature, String activationCode) {
    this.mail = mail;
    this.signature = signature;
    this.activationCode = activationCode;
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PRootData)prevalentSystem).register(mail, signature, activationCode);
  }

  public String getSerializationName() {
    return REGISTER;
  }

  public void read(SerializedInput input, Directory directory) {
    int version = input.readNotNullInt();
    if (version == V1) {
      mail = input.readBytes();
      signature = input.readBytes();
      activationCode = input.readJavaString();
    }
    if (version == V2) {
      mail = input.readBytes();
      signature = input.readBytes();
      activationCode = input.readUtf8String();
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.write(V2);
    output.writeBytes(mail);
    output.writeBytes(signature);
    output.writeUtf8String(activationCode);
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
