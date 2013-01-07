package com.budgetview.shared.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
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

public class SeriesValues {
  public static GlobType TYPE;

  @Key @Target(SeriesEntity.class)
  public static LinkField SERIES_ENTITY;

  @Key
  public static IntegerField MONTH;

  @Target(BudgetAreaEntity.class)
  public static LinkField BUDGET_AREA;

  public static DoubleField AMOUNT;
  public static DoubleField PLANNED_AMOUNT;
  public static DoubleField REMAINING_AMOUNT;
  public static DoubleField OVERRUN_AMOUNT;

  static {
    GlobTypeLoader.init(SeriesValues.class, "seriesValues");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.write(fieldValues.get(SERIES_ENTITY));
      output.write(fieldValues.get(MONTH));
      output.writeInteger(fieldValues.get(BUDGET_AREA));
      output.writeDouble(fieldValues.get(AMOUNT));
      output.writeDouble(fieldValues.get(PLANNED_AMOUNT));
      output.writeDouble(fieldValues.get(REMAINING_AMOUNT));
      output.writeDouble(fieldValues.get(OVERRUN_AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(data, fieldSetter);
      }
    }

    private void deserializeDataV1(byte[] data, FieldSetter fieldSetter) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SERIES_ENTITY, input.readNotNullInt());
      fieldSetter.set(MONTH, input.readNotNullInt());
      fieldSetter.set(BUDGET_AREA, input.readInteger());
      fieldSetter.set(AMOUNT, input.readDouble());
      fieldSetter.set(PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(REMAINING_AMOUNT, input.readDouble());
      fieldSetter.set(OVERRUN_AMOUNT, input.readDouble());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
