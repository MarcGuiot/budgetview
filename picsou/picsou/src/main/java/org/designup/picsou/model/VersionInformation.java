package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class VersionInformation {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static LongField CURRENT_SOFTWARE_VERSION;
  public static LongField LATEST_AVALAIBLE_SOFTWARE_VERSION;

  public static LongField CURRENT_JAR_VERSION;
  public static LongField LATEST_AVALAIBLE_JAR_VERSION;

  public static LongField CURRENT_BANK_CONFIG_VERSION;
  public static LongField LATEST_BANK_CONFIG_SOFTWARE_VERSION;

  static {
    GlobTypeLoader.init(VersionInformation.class, "versionInformation");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      outputStream.writeLong(values.get(CURRENT_SOFTWARE_VERSION));
      outputStream.writeLong(values.get(LATEST_AVALAIBLE_SOFTWARE_VERSION));
      outputStream.writeLong(values.get(CURRENT_JAR_VERSION));
      outputStream.writeLong(values.get(LATEST_AVALAIBLE_JAR_VERSION));
      outputStream.writeLong(values.get(CURRENT_BANK_CONFIG_VERSION));
      outputStream.writeLong(values.get(LATEST_BANK_CONFIG_SOFTWARE_VERSION));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(CURRENT_SOFTWARE_VERSION, input.readLong());
      fieldSetter.set(LATEST_AVALAIBLE_SOFTWARE_VERSION, input.readLong());
      fieldSetter.set(CURRENT_JAR_VERSION, input.readLong());
      fieldSetter.set(LATEST_AVALAIBLE_JAR_VERSION, input.readLong());
      fieldSetter.set(CURRENT_BANK_CONFIG_VERSION, input.readLong());
      fieldSetter.set(LATEST_BANK_CONFIG_SOFTWARE_VERSION, input.readLong());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}