package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.Provider;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class CloudProviderConnection {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_CONNECTION_ID;

  @Target(Bank.class)
  public static LinkField BANK;

  public static StringField BANK_NAME;

  @DefaultBoolean(false)
  public static BooleanField INITIALIZED;

  @DefaultBoolean(false)
  public static BooleanField PASSWORD_ERROR;

  @DefaultBoolean(false)
  public static BooleanField ACTION_NEEDED;

  static {
    TypeLoader.init(CloudProviderConnection.class, "cloudProviderConnection");
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(PROVIDER));
      outputStream.writeInteger(values.get(PROVIDER_CONNECTION_ID));
      outputStream.writeInteger(values.get(BANK));
      outputStream.writeUtf8String(values.get(BANK_NAME));
      outputStream.writeBoolean(values.get(INITIALIZED));
      outputStream.writeBoolean(values.get(PASSWORD_ERROR));
      outputStream.writeBoolean(values.get(ACTION_NEEDED));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 2) {
        deserializeV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeV1(fieldSetter, data);
      }
    }

    private void deserializeV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(PROVIDER, input.readInteger());
      fieldSetter.set(PROVIDER_CONNECTION_ID, input.readInteger());
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(BANK_NAME, input.readUtf8String());
      fieldSetter.set(INITIALIZED, input.readBoolean());
      fieldSetter.set(PASSWORD_ERROR, input.readBoolean());
      fieldSetter.set(ACTION_NEEDED, input.readBoolean());
    }

    private void deserializeV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(PROVIDER, input.readInteger());
      fieldSetter.set(PROVIDER_CONNECTION_ID, input.readInteger());
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(BANK_NAME, input.readUtf8String());
      fieldSetter.set(INITIALIZED, input.readBoolean());
      fieldSetter.set(PASSWORD_ERROR, input.readBoolean());
      fieldSetter.set(ACTION_NEEDED, false);
    }
  }
}
