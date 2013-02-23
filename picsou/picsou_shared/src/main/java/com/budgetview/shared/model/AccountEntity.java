package com.budgetview.shared.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class AccountEntity {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static StringField LABEL;

  @Target(MonthEntity.class)
  @NoObfuscation
  public static LinkField POSITION_MONTH;

  @NoObfuscation
  public static IntegerField POSITION_DAY;

  @NoObfuscation
  public static DoubleField POSITION;

  @NoObfuscation
  public static IntegerField ACCOUNT_TYPE;

  @NoObfuscation
  public static IntegerField SEQUENCE_NUMBER;

  public static Integer ACCOUNT_TYPE_MAIN = 1;
  public static Integer ACCOUNT_TYPE_SAVINGS = 2;

  static {
    GlobTypeLoader.init(AccountEntity.class, "accountEntity");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.write(fieldValues.get(ID));
      output.writeUtf8String(fieldValues.get(LABEL));
      output.writeInteger(fieldValues.get(POSITION_MONTH));
      output.writeInteger(fieldValues.get(POSITION_DAY));
      output.writeDouble(fieldValues.get(POSITION));
      output.writeInteger(fieldValues.get(ACCOUNT_TYPE));
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
      fieldSetter.set(LABEL, input.readUtf8String());
      fieldSetter.set(POSITION_MONTH, input.readInteger());
      fieldSetter.set(POSITION_DAY, input.readInteger());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(SEQUENCE_NUMBER, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
