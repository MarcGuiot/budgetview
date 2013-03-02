package com.budgetview.shared.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
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

public class TransactionValues {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(SeriesEntity.class)
  @NoObfuscation
  public static LinkField SERIES;

  @Target(AccountEntity.class)
  @NoObfuscation
  public static LinkField ACCOUNT;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static DoubleField AMOUNT;

  @Target(MonthEntity.class)
  @NoObfuscation
  public static LinkField BANK_MONTH;
  @NoObfuscation
  public static IntegerField BANK_DAY;

  @NoObfuscation
  public static IntegerField SEQUENCE_NUMBER;

  static {
    GlobTypeLoader.init(TransactionValues.class, "transactionValues");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.write(fieldValues.get(ID));
      output.writeInteger(fieldValues.get(SERIES));
      output.writeInteger(fieldValues.get(ACCOUNT));
      output.writeUtf8String(fieldValues.get(LABEL));
      output.writeDouble(fieldValues.get(AMOUNT));
      output.writeInteger(fieldValues.get(BANK_MONTH));
      output.writeInteger(fieldValues.get(BANK_DAY));
      output.writeInteger(fieldValues.get(SEQUENCE_NUMBER));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(data, fieldSetter);
      }
    }

    private void deserializeDataV1(byte[] data, FieldSetter fieldSetter) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readNotNullInt());
      fieldSetter.set(SERIES, input.readInteger());
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(LABEL, input.readUtf8String());
      fieldSetter.set(AMOUNT, input.readDouble());
      fieldSetter.set(BANK_MONTH, input.readInteger());
      fieldSetter.set(BANK_DAY, input.readInteger());
      fieldSetter.set(SEQUENCE_NUMBER, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
