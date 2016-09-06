package com.budgetview.server.cloud.model;

import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class ProviderAccount {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField PROVIDER_BANK_ID;

  public static StringField PROVIDER_BANK_NAME;

  public static StringField NAME;

  public static StringField NUMBER;

  public static DoubleField POSITION;

  public static IntegerField POSITION_MONTH;

  public static IntegerField POSITION_DAY;

  public static StringField ACCOUNT_TYPE;

  public static BooleanField DELETED;

  static {
    GlobTypeLoader.init(ProviderAccount.class, "providerAccount");
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(PROVIDER_BANK_ID));
      output.writeUtf8String(fieldValues.get(PROVIDER_BANK_NAME));
      output.writeUtf8String(fieldValues.get(NAME));
      output.writeUtf8String(fieldValues.get(NUMBER));
      output.writeDouble(fieldValues.get(POSITION));
      output.writeInteger(fieldValues.get(POSITION_MONTH));
      output.writeInteger(fieldValues.get(POSITION_DAY));
      output.writeUtf8String(fieldValues.get(ACCOUNT_TYPE));
      output.writeBoolean(fieldValues.get(DELETED));
      return serializedByteArrayOutput.toByteArray();
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(PROVIDER_BANK_ID, input.readInteger());
      fieldSetter.set(PROVIDER_BANK_NAME, input.readUtf8String());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(POSITION_MONTH, input.readInteger());
      fieldSetter.set(POSITION_DAY, input.readInteger());
      fieldSetter.set(ACCOUNT_TYPE, input.readUtf8String());
      fieldSetter.set(DELETED, input.readBoolean());
    }
  }
}
