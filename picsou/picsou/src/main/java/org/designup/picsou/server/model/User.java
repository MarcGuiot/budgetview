package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class User {

  public static GlobType TYPE;

  @Key
  public static StringField NAME;
  public static BooleanField AUTO_LOG;
  public static BlobField ENCRYPTED_PASSWORD;   // Chiffrement PBE a partir du mot de passe de l'utilisateur.
  public static BlobField LINK_INFO;  // information generee sur lequel on applique un PBE specific
  public static BooleanField IS_REGISTERED_USER;
  public final static byte LASTEST_VERSION = 3;

  static {
    GlobTypeLoader.init(User.class);
  }

  public static Glob read(SerializedInput input) {
    byte version = input.readByte();
    switch (version) {
      case 1:
        return readV1(input);
      case 2:
        return readV2(input);
      case LASTEST_VERSION:
        return readV3(input);
      default:
        throw new InvalidData("Reading version '" + version + "' not managed");
    }
  }

  public static void write(SerializedOutput output, Glob glob) {
    output.writeByte(LASTEST_VERSION);
    output.writeJavaString(glob.get(NAME));
    output.writeBoolean(glob.get(AUTO_LOG));
    output.writeBytes(glob.get(ENCRYPTED_PASSWORD));
    output.writeBytes(glob.get(LINK_INFO));
    output.writeBoolean(glob.get(IS_REGISTERED_USER));
  }


  private static Glob readV1(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(NAME, input.readJavaString());
    builder.set(ENCRYPTED_PASSWORD, input.readBytes());
    builder.set(LINK_INFO, input.readBytes());
    builder.set(IS_REGISTERED_USER, input.readBoolean());
    builder.set(AUTO_LOG, false);
    return builder.get();
  }

  private static Glob readV2(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(NAME, input.readJavaString());
    builder.set(ENCRYPTED_PASSWORD, input.readBytes());
    builder.set(LINK_INFO, input.readBytes());
    builder.set(IS_REGISTERED_USER, input.readBoolean());
    Boolean hasPassword = input.readBoolean();
    builder.set(AUTO_LOG, false);
    return builder.get();
  }

  private static Glob readV3(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(NAME, input.readJavaString());
    builder.set(AUTO_LOG, input.readBoolean());
    builder.set(ENCRYPTED_PASSWORD, input.readBytes());
    builder.set(LINK_INFO, input.readBytes());
    builder.set(IS_REGISTERED_USER, input.readBoolean());
    return builder.get();
  }
}
