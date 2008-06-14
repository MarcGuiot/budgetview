package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class HiddenAccount {
  public static GlobType TYPE;
  @Key
  public static IntegerField ID;
  @Key
  public static IntegerField HIDDEN_USER_ID;
  public static BlobField CRYPTED_INFO;

  private static final byte V1 = 1;

  static {
    GlobTypeLoader.init(HiddenAccount.class);
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
    write(output, glob, (byte)-1);
  }

  static void write(SerializedOutput output, Glob glob, byte version) {
    switch (version) {
      case -1:
      case V1:
        writeV1(output, glob);
        return;
      default:
        throw new InvalidData("Writing version '" + version + "' not managed");
    }
  }

  private static Glob readV1(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(ID, input.readInteger());
    builder.set(HIDDEN_USER_ID, -1);
    builder.set(CRYPTED_INFO, input.readBytes());
    return builder.get();
  }

  private static void writeV1(SerializedOutput output, Glob glob) {
    output.writeByte(V1);
    output.writeInteger(glob.get(ID));
    output.writeBytes(glob.get(CRYPTED_INFO));
  }

}
