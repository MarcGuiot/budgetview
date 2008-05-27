package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.MaxSize;
import org.crossbowlabs.globs.metamodel.fields.BlobField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

public class HiddenTransaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  @Key
  public static IntegerField HIDDEN_USER_ID;
  @MaxSize(256)
  public static StringField HIDDEN_LABEL;  // To be removed after upgrade
  public static IntegerField TRANSACTION_TYPE_ID;    // To be removed after upgrade
  public static BlobField ENCRYPTED_INFO;

  static final byte V1 = 1;
  static final byte V2 = 2;

  static {
    GlobTypeLoader.init(HiddenTransaction.class);
  }

  public static Glob read(SerializedInput input) {
    byte version = input.readByte();
    switch (version) {
      case-1:
      case V1:
        return readV1(input);
      case V2:
        return readV2(input);
      default:
        throw new InvalidData("Reading version '" + version + "' not managed");
    }
  }

  public static void write(SerializedOutput output, Glob glob) {
    write(output, glob, (byte) -1);
  }

  static void write(SerializedOutput output, Glob glob, byte version) {
    switch (version) {
      case V1 :
        writeV1(output, glob);
        return;
      case-1:
      case V2:
        writeV2(output, glob);
        return;
      default:
        throw new InvalidData("Writing version '" + version + "' not managed");
    }
  }

  private static Glob readV1(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(ID, input.readInteger());
    builder.set(HIDDEN_USER_ID, -1);
    builder.set(HIDDEN_LABEL, input.readString());
    builder.set(TRANSACTION_TYPE_ID, input.readInteger());
    builder.set(ENCRYPTED_INFO, input.readBytes());
    return builder.get();
  }

  private static Glob readV2(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(ID, input.readInteger());
    builder.set(HIDDEN_USER_ID, -1);
    builder.set(ENCRYPTED_INFO, input.readBytes());
    return builder.get();
  }

  private static void writeV2(SerializedOutput output, Glob glob) {
    output.writeByte(V2);
    output.writeInteger(glob.get(ID));
    output.writeBytes(glob.get(ENCRYPTED_INFO));
  }

  private static void writeV1(SerializedOutput output, Glob glob) {
    output.writeByte(V1);
    output.writeInteger(glob.get(ID));
    output.writeString(glob.get(HIDDEN_LABEL));
    output.writeInteger(glob.get(TRANSACTION_TYPE_ID));
    output.writeBytes(glob.get(ENCRYPTED_INFO));
  }
}
