package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class CurrentMonth {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField MONTH_ID;

  public static IntegerField DAY;

  public static org.globsframework.model.Key KEY;

  static {
    GlobTypeLoader.init(CurrentMonth.class, "currentMonth");
    KEY = org.globsframework.model.Key.create(TYPE, 0);
  }

  public static Integer get(GlobRepository repository) {
    return repository.get(CurrentMonth.KEY).get(MONTH_ID);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      outputStream.writeInteger(values.get(MONTH_ID));
      outputStream.writeInteger(values.get(DAY));
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
      fieldSetter.set(MONTH_ID, input.readInteger());
      fieldSetter.set(DAY, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }
  }
}