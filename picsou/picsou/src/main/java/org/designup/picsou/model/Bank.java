package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultString;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Bank {
  public static final int GENERIC_BANK_ID = -123456;

  public static GlobType TYPE;

  @Key @NoObfuscation
  public static IntegerField ID;

  @NamingField @NoObfuscation
  public static StringField NAME;

  @DefaultString("") @NoObfuscation
  public static StringField DOWNLOAD_URL;

  static {
    GlobTypeLoader.init(Bank.class, "bank");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeUtf8String(values.get(DOWNLOAD_URL));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readJavaString());
      fieldSetter.set(DOWNLOAD_URL, input.readJavaString());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(DOWNLOAD_URL, input.readUtf8String());
    }

    public int getWriteVersion() {
      return 2;
    }
  }

}
