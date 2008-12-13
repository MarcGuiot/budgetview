package org.designup.picsou.model;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class CurrentMonth {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField LAST_TRANSACTION_MONTH;

  public static IntegerField LAST_TRANSACTION_DAY;

  public static IntegerField CURRENT_MONTH;

  public static IntegerField CURRENT_DAY;

  public static org.globsframework.model.Key KEY;

  static {
    GlobTypeLoader.init(CurrentMonth.class, "currentMonth");
    KEY = org.globsframework.model.Key.create(TYPE, 0);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      outputStream.writeInteger(values.get(LAST_TRANSACTION_MONTH));
      outputStream.writeInteger(values.get(LAST_TRANSACTION_DAY));
      outputStream.writeInteger(values.get(CURRENT_MONTH));
      outputStream.writeInteger(values.get(CURRENT_DAY));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_MONTH, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_DAY, input.readInteger());
      fieldSetter.set(CURRENT_MONTH, TimeService.getCurrentMonth());
      fieldSetter.set(CURRENT_DAY, TimeService.getCurrentDay());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_MONTH, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_DAY, input.readInteger());
      fieldSetter.set(CURRENT_MONTH, input.readInteger());
      fieldSetter.set(CURRENT_DAY, input.readInteger());
    }

    public int getWriteVersion() {
      return 2;
    }
  }
}