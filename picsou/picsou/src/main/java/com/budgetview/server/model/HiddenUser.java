package com.budgetview.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.MaxSize;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class HiddenUser {
  public static GlobType TYPE;
  @Key
  @MaxSize(256)
  public static StringField ENCRYPTED_LINK_INFO; // encodage base64 du LINK_INFO du User retourné par le client
  public static IntegerField USER_ID;
  private static final byte V1 = 1;
  private static final byte V2 = 2;

  static {
    GlobTypeLoader.init(HiddenUser.class);
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

  public static void write(SerializedOutput output, Glob glob, byte version) {
    switch (version) {
      case -1:
      case V2:
        writeV2(output, glob);
        return;
      case V1:
        writeV1(output, glob);
        return;
      default:
        throw new InvalidData("Writing version '" + version + "' not managed");
    }
  }

  private static Glob readV1(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(USER_ID, input.readInteger());
    builder.set(ENCRYPTED_LINK_INFO, Strings.removeNewLine(input.readJavaString()));
    return builder.get();
  }

  private static Glob readV2(SerializedInput input) {
    GlobBuilder builder = GlobBuilder.init(TYPE);
    builder.set(USER_ID, input.readInteger());
    builder.set(ENCRYPTED_LINK_INFO, Strings.removeNewLine(input.readUtf8String()));
    return builder.get();
  }

  private static void writeV1(SerializedOutput output, Glob glob) {
    output.writeByte(V1);
    output.writeInteger(glob.get(USER_ID));
    output.writeJavaString(glob.get(ENCRYPTED_LINK_INFO));
  }

  private static void writeV2(SerializedOutput output, Glob glob) {
    output.writeByte(V2);
    output.writeInteger(glob.get(USER_ID));
    output.writeUtf8String(glob.get(ENCRYPTED_LINK_INFO));
  }
}
