package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.BlobField;
import org.crossbowlabs.globs.metamodel.fields.BooleanField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

public class User {

  public static GlobType TYPE;

  @Key
  public static StringField NAME;
  public static BlobField ENCRYPTED_PASSWORD;   // Chiffrement PBE a partir du mot de passe de l'utilisateur.
  public static BlobField LINK_INFO;  // information generee sur lequel on applique un PBE specific
  //  public static StringField TEMPORARY_PASSWORD;  // mot de passe en claire pour la premiere connection
  public static BooleanField IS_REGISTERED_USER;
  private static final byte V1 = 1;
  // (avec un salt et count different de celui pour le mot de passe
  // de l'utilisateur)
  // et qui creer l'association avec le Hidden user

  static {
    GlobTypeLoader.init(User.class);
  }

  public static Glob read(SerializedInput input) {
    byte version = input.readByte();
    switch (version) {
      case V1:
        return readV1(input);
      default:
        throw new InvalidData("Reading version '" + version + "' not managed");
    }
  }

  public static void write(SerializedOutput output, Glob glob) {
    write(output, glob, (byte) -1);
  }

  static void write(SerializedOutput output, Glob glob, byte version) {
    switch (version) {
      case-1:
      case V1:
        writeV1(output, glob);
        return;
      default:
        throw new InvalidData("Writing version '" + version + "' not managed");
    }
  }

  private static Glob readV1(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(NAME, input.readString());
    builder.set(ENCRYPTED_PASSWORD, input.readBytes());
    builder.set(LINK_INFO, input.readBytes());
    builder.set(IS_REGISTERED_USER, input.readBoolean());
    return builder.get();
  }

  private static void writeV1(SerializedOutput output, Glob glob) {
    output.writeByte(V1);
    output.writeString(glob.get(NAME));
    output.writeBytes(glob.get(ENCRYPTED_PASSWORD));
    output.writeBytes(glob.get(LINK_INFO));
    output.writeBoolean(glob.get(IS_REGISTERED_USER));
  }
}
