package com.budgetview.persistence.prevayler.users;

import com.budgetview.client.exceptions.IdentificationFailed;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.persistence.prevayler.CustomSerializable;
import com.budgetview.persistence.prevayler.CustomSerializableFactory;
import com.budgetview.session.model.HiddenUser;
import com.budgetview.session.model.User;
import com.budgetview.session.states.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.TransactionWithQuery;

import java.util.Date;

public class CreateUserAndHiddenUser implements TransactionWithQuery, CustomSerializable {
  private String name;
  private boolean autoLog;
  private boolean isRegisteredUser;
  private byte[] encryptedPassword;
  private byte[] linkInfo;
  private Integer newUserId = null;
  private String encryptedLinkInfo;
  private static final String TRANSACTION_NAME = "CreateUser";

  public CreateUserAndHiddenUser(String name, boolean autoLog, boolean isRegisteredUser,
                                 byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo,
                                 Integer newUserId) {
    this.name = name;
    this.autoLog = autoLog;
    this.isRegisteredUser = isRegisteredUser;
    this.encryptedPassword = cryptedPassword;
    this.linkInfo = linkInfo;
    this.newUserId = newUserId;
    this.encryptedLinkInfo = Encoder.byteToString(cryptedLinkInfo);
  }

  private CreateUserAndHiddenUser() {
  }

  public Object executeAndQuery(Object prevalentSystem, Date executionTime) {
    PRootData rootData = ((PRootData)prevalentSystem);
    if (rootData.getUser(name) != null) {
      throw new UserAlreadyExists("User '" + name + "' already registered");
    }
    if (rootData.getHiddenUser(encryptedLinkInfo) != null) {
      throw new IdentificationFailed("Duplicate info");
    }
    Glob user = GlobBuilder.init(User.TYPE)
      .set(User.ENCRYPTED_PASSWORD, encryptedPassword)
      .set(User.NAME, name)
      .set(User.AUTO_LOG, autoLog)
      .set(User.LINK_INFO, linkInfo)
      .set(User.IS_REGISTERED_USER, isRegisteredUser)
      .get();
    rootData.addUser(name, user);

    if (newUserId == null) { // for backward compatibility
      newUserId = rootData.getNewUserId(executionTime, name);
    }
    Glob hiddenUser = GlobBuilder.init(HiddenUser.TYPE)
      .set(HiddenUser.ENCRYPTED_LINK_INFO, encryptedLinkInfo)
      .set(HiddenUser.USER_ID, newUserId)
      .get();
    rootData.addHiddenUser(encryptedLinkInfo, hiddenUser);
    return new Persistence.UserInfo(newUserId, isRegisteredUser);
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
      case 2:
        readV2(input);
        break;
      case 3:
        readV3(input);
        break;
      case 4:
        readV4(input);
        break;
      case 5:
        readV5(input);
        break;
      default:
        throw new UnexpectedApplicationState("version " + version + " not managed");
    }
  }

  private void readV1(SerializedInput input) {
    name = input.readJavaString();
    encryptedPassword = input.readBytes();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readJavaString();
    isRegisteredUser = input.readBoolean();
  }

  private void readV2(SerializedInput input) {
    name = input.readUtf8String();
    encryptedPassword = input.readBytes();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readJavaString();
    isRegisteredUser = input.readBoolean();
  }

  private void readV3(SerializedInput input) {
    name = input.readUtf8String();
    autoLog = input.readBoolean();
    encryptedPassword = input.readBytes();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readJavaString();
    isRegisteredUser = input.readBoolean();
  }

  private void readV4(SerializedInput input) {
    name = input.readUtf8String();
    autoLog = input.readBoolean();
    encryptedPassword = input.readBytes();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readJavaString();
    isRegisteredUser = input.readBoolean();
    newUserId = input.readInteger();
  }

  private void readV5(SerializedInput input) {
    name = input.readUtf8String();
    autoLog = input.readBoolean();
    encryptedPassword = input.readBytes();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readUtf8String();
    isRegisteredUser = input.readBoolean();
    newUserId = input.readInteger();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(5);
    output.writeUtf8String(name);
    output.writeBoolean(autoLog);
    output.writeBytes(encryptedPassword);
    output.writeBytes(linkInfo);
    output.writeUtf8String(encryptedLinkInfo);
    output.writeBoolean(isRegisteredUser);
    output.writeInteger(newUserId);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new CreateUserAndHiddenUser();
    }
  }
}
