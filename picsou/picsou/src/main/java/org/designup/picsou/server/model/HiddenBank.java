package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

public class HiddenBank {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  @Key
  public static IntegerField HIDDEN_USER_ID;

  private static final byte V1 = 1;

  static {
    GlobTypeLoader.init(HiddenBank.class);
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
    builder.set(ID, input.readInteger());
    builder.set(HIDDEN_USER_ID, -1);
    return builder.get();
  }

  private static void writeV1(SerializedOutput output, Glob glob) {
    output.writeByte(V1);
    output.writeInteger(glob.get(ID));
  }


}
