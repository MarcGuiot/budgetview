package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
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
  
  @Target(BankFormat.class) @NoObfuscation
  public static LinkField BANK_FORMAT;

  public static org.globsframework.model.Key GENERIC_BANK_KEY;

  @DefaultBoolean(false)
  @NoObfuscation
  public static BooleanField SYNCHRO_ENABLE;

  static {
    GlobTypeLoader.init(Bank.class, "bank");
    GENERIC_BANK_KEY = org.globsframework.model.Key.create(TYPE, GENERIC_BANK_ID);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeUtf8String(values.get(DOWNLOAD_URL));
      outputStream.write(values.get(SYNCHRO_ENABLE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
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

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(DOWNLOAD_URL, input.readUtf8String());
      fieldSetter.set(SYNCHRO_ENABLE, input.readBoolean());
    }

    public int getWriteVersion() {
      return 3;
    }
  }

}
