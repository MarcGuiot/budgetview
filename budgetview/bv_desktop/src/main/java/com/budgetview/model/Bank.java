package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.Provider;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class Bank {
  public static final int GENERIC_BANK_ID = -123456;

  public static GlobType TYPE;

  @Key @NoObfuscation
  public static IntegerField ID;

  @NamingField @NoObfuscation
  public static StringField NAME;

  @NamingField @NoObfuscation
  public static StringField SHORT_NAME;

  @DefaultString("") @NoObfuscation
  public static StringField COUNTRY;

  @Target(Provider.class) @NoObfuscation
  public static LinkField PROVIDER;

  @NoObfuscation
  public static IntegerField PROVIDER_ID;

  @DefaultString("") @NoObfuscation
  public static StringField URL;

  @NamingField @NoObfuscation
  public static StringField ICON;

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
    TypeLoader.init(Bank.class, "bank");
    GENERIC_BANK_KEY = org.globsframework.model.Key.create(TYPE, GENERIC_BANK_ID);
  }

  public static Integer findIdByProviderId(int provider, int providerBankId, GlobRepository repository) {
    Glob bank = findByProviderId(provider, providerBankId, repository);
    return bank != null ? bank.get(Bank.ID) : null;
  }

  public static Glob findByProviderId(int provider, int providerBankId, GlobRepository repository) {
    GlobList banks = repository.getAll(TYPE, and(fieldEquals(PROVIDER, provider),
                                                 fieldEquals(PROVIDER_ID, providerBankId)));
    return banks.isEmpty() ? null : banks.getFirst();
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 7;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return fieldValues.isTrue(USER_CREATED);
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeUtf8String(values.get(SHORT_NAME));
      outputStream.writeUtf8String(values.get(URL));
      outputStream.writeUtf8String(values.get(ICON));
      outputStream.writeUtf8String(values.get(COUNTRY));
      outputStream.writeUtf8String(values.get(DOWNLOAD_URL));
      outputStream.writeUtf8String(values.get(FID));
      outputStream.writeUtf8String(values.get(ORG));
      outputStream.writeBoolean(values.get(INVALID_POSITION));
      outputStream.writeBoolean(values.get(OFX_DOWNLOAD));
      outputStream.writeBoolean(values.get(SYNCHRO_ENABLED));
      outputStream.writeBoolean(values.get(USER_CREATED));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 7) {
        deserializeDataV7(fieldSetter, data);
      }
      else if (version == 6) {
        deserializeDataV6(fieldSetter, data);
      }
      else if (version == 5) {
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

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(SHORT_NAME, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
      fieldSetter.set(ICON, input.readUtf8String());
      fieldSetter.set(COUNTRY, input.readUtf8String());
      fieldSetter.set(DOWNLOAD_URL, input.readUtf8String());
      fieldSetter.set(FID, input.readUtf8String());
      fieldSetter.set(ORG, input.readUtf8String());
      fieldSetter.set(INVALID_POSITION, input.readBoolean());
      fieldSetter.set(OFX_DOWNLOAD, input.readBoolean());
      fieldSetter.set(SYNCHRO_ENABLED, input.readBoolean());
      fieldSetter.set(USER_CREATED, input.readBoolean());
    }

    private void deserializeDataV6(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(SHORT_NAME, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
      fieldSetter.set(ICON, input.readUtf8String());
      fieldSetter.set(DOWNLOAD_URL, input.readUtf8String());
      fieldSetter.set(FID, input.readUtf8String());
      fieldSetter.set(ORG, input.readUtf8String());
      fieldSetter.set(INVALID_POSITION, input.readBoolean());
      fieldSetter.set(OFX_DOWNLOAD, input.readBoolean());
      fieldSetter.set(SYNCHRO_ENABLED, input.readBoolean());
      fieldSetter.set(USER_CREATED, input.readBoolean());
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
