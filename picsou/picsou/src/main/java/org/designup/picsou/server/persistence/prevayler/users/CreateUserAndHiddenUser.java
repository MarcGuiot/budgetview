package org.designup.picsou.server.persistence.prevayler.users;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.utils.serialization.Encoder;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.designup.picsou.server.session.Persistence;
import org.prevayler.TransactionWithQuery;

import java.util.Date;

public class CreateUserAndHiddenUser implements TransactionWithQuery, CustomSerializable {
  private String name;
  private boolean isRegisteredUser;
  private byte[] encryptedPassword;
  private byte[] linkInfo;
  private String encryptedLinkInfo;
  private static final byte V1 = 1;
  private static final String TRANSACTION_NAME = "CreateUser";

  public CreateUserAndHiddenUser(String name, boolean isRegisteredUser,
                                 byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo) {
    this.name = name;
    this.isRegisteredUser = isRegisteredUser;
    this.encryptedPassword = cryptedPassword;
    this.linkInfo = linkInfo;
    this.encryptedLinkInfo = Encoder.b64Decode(cryptedLinkInfo);
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
      .set(User.LINK_INFO, linkInfo)
      .set(User.IS_REGISTERED_USER, isRegisteredUser)
      .get();
    rootData.addUser(name, user);

    int newUserId = rootData.getNewUserId(executionTime, name);
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
      case V1:
        readV1(input);
        break;
      default:
        throw new UnexpectedApplicationState("version " + version + " not managed");
    }
  }

  private void readV1(SerializedInput input) {
    name = input.readString();
    encryptedPassword = input.readBytes();
    linkInfo = input.readBytes();
    encryptedLinkInfo = input.readString();
    isRegisteredUser = input.readBoolean();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V1);
    output.writeString(name);
    output.writeBytes(encryptedPassword);
    output.writeBytes(linkInfo);
    output.writeString(encryptedLinkInfo);
    output.writeBoolean(isRegisteredUser);
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
