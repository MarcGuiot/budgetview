package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.TransactionWithQuery;

import java.util.Date;
import java.util.Arrays;

public class RenameUserAndHiddenUser implements TransactionWithQuery, CustomSerializable {
  private String newName;
  private byte[] newCryptedPassword;
  private byte[] newLinkInfo;
  private String newCryptedLinkInfo;
  private String name;
  private boolean autoLog;
  private boolean isRegisteredUser;
  private byte[] linkInfo;
  private Integer newUserId = null;
  private String encryptedLinkInfo;
  private static final String TRANSACTION_NAME = "RenameUser";

  public RenameUserAndHiddenUser(boolean autoLog, boolean isRegisteredUser, String newName,
                                 byte[] newCryptedPassword, byte[] newLinkInfo, byte[] newCryptedLinkInfo,
                                 String name, byte[] linkInfo, byte[] cryptedLinkInfo, Integer newUserId) {
    this.newName = newName;
    this.newCryptedPassword = newCryptedPassword;
    this.newLinkInfo = newLinkInfo;
    this.newCryptedLinkInfo = Encoder.byteToString(newCryptedLinkInfo);
    this.name = name;
    this.autoLog = autoLog;
    this.isRegisteredUser = isRegisteredUser;
    this.linkInfo = linkInfo;
    this.newUserId = newUserId;
    this.encryptedLinkInfo = Encoder.byteToString(cryptedLinkInfo);
  }

  private RenameUserAndHiddenUser() {
  }

  public Object executeAndQuery(Object prevalentSystem, Date executionTime) {
    PRootData rootData = ((PRootData)prevalentSystem);
    if (rootData.getUser(name) == null) {
      return new UserNotRegistered("User '" + name + "' unknown");
    }
    Glob currentUser = rootData.getUser(name);
    if (!Arrays.equals(currentUser.get(User.LINK_INFO), linkInfo)){
      return new IdentificationFailed("User '" + name + "' known but bad linkInfo");
    }
    if (rootData.getUser(newName) != null) {
      return new UserAlreadyExists("User '" + newName + "' already exist");
    }
    if (rootData.getHiddenUser(encryptedLinkInfo) == null) {
      return new IdentificationFailed("missing associated info");
    }
    if (rootData.getHiddenUser(newCryptedLinkInfo) != null) {
      return new IdentificationFailed("Duplicate info");
    }

    Glob user = GlobBuilder.init(User.TYPE)
      .set(User.ENCRYPTED_PASSWORD, newCryptedPassword)
      .set(User.NAME, newName)
      .set(User.AUTO_LOG, autoLog)
      .set(User.LINK_INFO, newLinkInfo)
      .set(User.IS_REGISTERED_USER, isRegisteredUser)
      .get();
    rootData.addUser(newName, user);

    Glob hiddenUser = GlobBuilder.init(HiddenUser.TYPE)
      .set(HiddenUser.ENCRYPTED_LINK_INFO, newCryptedLinkInfo)
      .set(HiddenUser.USER_ID, newUserId)
      .get();
    rootData.addHiddenUser(newCryptedLinkInfo, hiddenUser);
    rootData.removeHiddenUser(encryptedLinkInfo);
    rootData.removeUser(name);
    return null;
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
    name = input.readUtf8String();
    autoLog = input.readBoolean();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readUtf8String();
    isRegisteredUser = input.readBoolean();
    newUserId = input.readInteger();
    newName = input.readUtf8String();
    newCryptedPassword = input.readBytes();
    newLinkInfo = input.readBytes();
    newCryptedLinkInfo = input.readUtf8String();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(1);
    output.writeUtf8String(name);
    output.writeBoolean(autoLog);
    output.writeBytes(linkInfo);
    output.writeUtf8String(encryptedLinkInfo);
    output.writeBoolean(isRegisteredUser);
    output.writeInteger(newUserId);
    output.writeUtf8String(newName);
    output.writeBytes(newCryptedPassword);
    output.writeBytes(newLinkInfo);
    output.writeUtf8String(newCryptedLinkInfo);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new RenameUserAndHiddenUser();
    }
  }
}