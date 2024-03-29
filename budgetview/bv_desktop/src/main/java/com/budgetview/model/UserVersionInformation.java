package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class UserVersionInformation {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField CURRENT_SOFTWARE_VERSION;
  public static LongField CURRENT_JAR_VERSION;
  public static LongField CURRENT_BANK_CONFIG_VERSION;

  static {
    TypeLoader.init(UserVersionInformation.class, "versionInformation");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }
  public static class Serializer implements GlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      outputStream.writeUtf8String(values.get(CURRENT_SOFTWARE_VERSION));
      outputStream.writeLong(values.get(CURRENT_JAR_VERSION));
      outputStream.writeLong(values.get(CURRENT_BANK_CONFIG_VERSION));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(CURRENT_SOFTWARE_VERSION, "0.9");
      fieldSetter.set(CURRENT_JAR_VERSION, input.readLong());
      long latestJar =  input.readLong();
      fieldSetter.set(CURRENT_BANK_CONFIG_VERSION, input.readLong());
      long latestConfig = input.readLong();
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(CURRENT_SOFTWARE_VERSION, input.readUtf8String());
      String version =  input.readUtf8String();
      fieldSetter.set(CURRENT_JAR_VERSION, input.readLong());
      long latestJar =  input.readLong();
      fieldSetter.set(CURRENT_BANK_CONFIG_VERSION, input.readLong());
      long latestConfig = input.readLong();
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(CURRENT_SOFTWARE_VERSION, input.readUtf8String());
      fieldSetter.set(CURRENT_JAR_VERSION, input.readLong());
      fieldSetter.set(CURRENT_BANK_CONFIG_VERSION, input.readLong());
    }

    public int getWriteVersion() {
      return 3;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }

}