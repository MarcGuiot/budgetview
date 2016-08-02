package com.budgetview.persistence.prevayler.users;

import com.budgetview.persistence.prevayler.CustomSerializable;
import com.budgetview.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.util.Date;

public class DeleteUserAndHiddenUser implements Transaction, CustomSerializable {
  private String name;
  private String cryptedLinkInfo;
  private static final byte V1 = 1;
  private static final byte V2 = 2;
  private static final byte V3 = 3;
  private static final String TRANSACTION_NAME = "DeleteUser";

  public DeleteUserAndHiddenUser(String name, byte[] cryptedLinkInfo) {
    this.name = name;
    this.cryptedLinkInfo = Encoder.byteToString(cryptedLinkInfo);
  }

  private DeleteUserAndHiddenUser() {
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    PRootData rootData = ((PRootData)prevalentSystem);
    rootData.removeUser(name);
    rootData.removeHiddenUser(cryptedLinkInfo);
  }

  public String getSerializationName() {
    return TRANSACTION_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    switch (version) {
      case V1:
        readV1(input);
        break;
      case V2:
        readV2(input);
        break;
      case V3:
        readV3(input);
        break;
      default:
        throw new UnexpectedApplicationState("version " + version + " not managed");
    }
  }

  private void readV1(SerializedInput input) {
    name = input.readJavaString();
    cryptedLinkInfo = input.readJavaString();
  }

  private void readV2(SerializedInput input) {
    name = input.readUtf8String();
    cryptedLinkInfo = input.readJavaString();
  }

  private void readV3(SerializedInput input) {
    name = input.readUtf8String();
    cryptedLinkInfo = input.readUtf8String();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V3);
    output.writeUtf8String(name);
    output.writeUtf8String(cryptedLinkInfo);
  }

  public static CustomSerializableFactory getFactory() {
    return new DeleteUserAndHiddenUser.Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new DeleteUserAndHiddenUser();
    }
  }
}
