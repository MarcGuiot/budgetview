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
  public static StringField URL;

  @DefaultString("") @NoObfuscation
  public static StringField DOWNLOAD_URL;

  @DefaultString("") @NoObfuscation
  public static StringField ORG;

  @DefaultString("") @NoObfuscation
  public static StringField FID;

  @Target(BankFormat.class) @NoObfuscation
  public static LinkField BANK_FORMAT;

  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField INVALID_POSITION;

  @NoObfuscation
  public static BooleanField OFX_DOWNLOAD;

  @DefaultBoolean(false)
  @NoObfuscation
  public static BooleanField SYNCHRO_ENABLED;

  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField USER_CREATED;

  public static org.globsframework.model.Key GENERIC_BANK_KEY;

  static {
    GlobTypeLoader.init(Bank.class, "bank");
    GENERIC_BANK_KEY = org.globsframework.model.Key.create(TYPE, GENERIC_BANK_ID);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 5;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return fieldValues.isTrue(USER_CREATED);
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeUtf8String(values.get(URL));
      outputStream.writeUtf8String(values.get(DOWNLOAD_URL));
      outputStream.writeUtf8String(values.get(FID));
      outputStream.writeUtf8String(values.get(ORG));
      outputStream.writeBoolean(values.get(INVALID_POSITION));
      outputStream.writeBoolean(values.get(OFX_DOWNLOAD));
      outputStream.writeBoolean(values.get(SYNCHRO_ENABLED));
      outputStream.writeBoolean(values.get(USER_CREATED));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 5) {
        deserializeDataV5(fieldSetter, data);
      }
      else if (version == 4) {
        deserializeDataV4(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
      fieldSetter.set(DOWNLOAD_URL, input.readUtf8String());
      fieldSetter.set(FID, input.readUtf8String());
      fieldSetter.set(ORG, input.readUtf8String());
      fieldSetter.set(INVALID_POSITION, input.readBoolean());
      fieldSetter.set(OFX_DOWNLOAD, input.readBoolean());
      fieldSetter.set(SYNCHRO_ENABLED, input.readBoolean());
      fieldSetter.set(USER_CREATED, input.readBoolean());
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
      fieldSetter.set(DOWNLOAD_URL, input.readUtf8String());
      fieldSetter.set(FID, input.readUtf8String());
      fieldSetter.set(ORG, input.readUtf8String());
      fieldSetter.set(INVALID_POSITION, input.readBoolean());
      fieldSetter.set(OFX_DOWNLOAD, input.readBoolean());
      fieldSetter.set(SYNCHRO_ENABLED, input.readBoolean());
      fieldSetter.set(USER_CREATED, false);
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      String url = input.readUtf8String();
      fieldSetter.set(URL, url);
      fieldSetter.set(DOWNLOAD_URL, url);
      fieldSetter.set(SYNCHRO_ENABLED, input.readBoolean());
      fieldSetter.set(USER_CREATED, false);
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
      fieldSetter.set(USER_CREATED, false);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readJavaString());
      fieldSetter.set(URL, input.readJavaString());
      fieldSetter.set(USER_CREATED, false);
    }
  }

}
