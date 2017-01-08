package com.budgetview.server.cloud.model;

import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class ProviderTransaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(ProviderAccount.class)
  public static LinkField ACCOUNT;

  public static StringField LABEL;

  public static StringField ORIGINAL_LABEL;

  public static DoubleField AMOUNT;

  public static DateField OPERATION_DATE;

  public static DateField BANK_DATE;

  public static IntegerField DEFAULT_SERIES_ID;

  public static StringField PROVIDER_CATEGORY_NAME;

  public static BooleanField DELETED;

  static {
    GlobTypeLoader.init(ProviderTransaction.class, "providerTransaction");
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
      output.writeInteger(fieldValues.get(ACCOUNT));
      output.writeUtf8String(fieldValues.get(LABEL));
      output.writeUtf8String(fieldValues.get(ORIGINAL_LABEL));
      output.writeDouble(fieldValues.get(AMOUNT));
      output.writeDate(fieldValues.get(OPERATION_DATE));
      output.writeDate(fieldValues.get(BANK_DATE));
      output.writeInteger(fieldValues.get(DEFAULT_SERIES_ID));
      output.writeUtf8String(fieldValues.get(PROVIDER_CATEGORY_NAME));
      output.writeBoolean(fieldValues.get(DELETED));
      return serializedByteArrayOutput.toByteArray();
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(LABEL, input.readUtf8String());
      fieldSetter.set(ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(AMOUNT, input.readDouble());
      fieldSetter.set(OPERATION_DATE, input.readDate());
      fieldSetter.set(BANK_DATE, input.readDate());
      fieldSetter.set(DEFAULT_SERIES_ID, input.readInteger());
      fieldSetter.set(PROVIDER_CATEGORY_NAME, input.readUtf8String());
      fieldSetter.set(DELETED, input.readBoolean());
    }
  }
}
