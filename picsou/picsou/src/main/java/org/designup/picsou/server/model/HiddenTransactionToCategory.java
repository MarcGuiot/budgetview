package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class HiddenTransactionToCategory {
  public static GlobType TYPE;

  @Key
  public static IntegerField HIDDEN_USER_ID;
  @Key
  public static IntegerField TRANSACTION_ID;
  @Key
  public static IntegerField CATEGORY_ID;

  private static final byte V1 = 1;
  private static final byte V2 = 2;

  static {
    GlobTypeLoader.init(HiddenTransactionToCategory.class);
  }

  public static Glob read(SerializedInput input) {
    byte version = input.readByte();
    switch (version) {
      case V1:
        return readV1(input);
      case V2:
        return readV2(input);
      default:
        throw new InvalidData("Reading version '" + version + "' not managed");
    }
  }

  public static void write(SerializedOutput output, Glob glob) {
    write(output, glob, (byte)-1);
  }

  static void write(SerializedOutput output, Glob glob, byte version) {
    switch (version) {
      case V1:
        throw new InvalidData("V1 not supported");
      case -1:
      case V2:
        writeV2(output, glob);
        return;
      default:
        throw new InvalidData("Writing version '" + version + "' not managed");
    }
  }

  private static Glob readV1(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(HIDDEN_USER_ID, input.readInteger());
    builder.set(TRANSACTION_ID, input.readInteger());
    builder.set(CATEGORY_ID, input.readInteger());
    return builder.get();
  }

  private static Glob readV2(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(TRANSACTION_ID, input.readInteger());
    builder.set(CATEGORY_ID, input.readInteger());
    builder.set(HIDDEN_USER_ID, -1);
    return builder.get();
  }

  private static void writeV2(SerializedOutput output, Glob glob) {
    output.writeByte(V2);
    output.writeInteger(glob.get(TRANSACTION_ID));
    output.writeInteger(glob.get(CATEGORY_ID));
  }
}
