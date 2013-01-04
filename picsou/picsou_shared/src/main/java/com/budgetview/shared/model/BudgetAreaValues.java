package com.budgetview.shared.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class BudgetAreaValues {
  public static GlobType TYPE;

  @Key @Target(BudgetAreaEntity.class)
  public static LinkField BUDGET_AREA;

  @Key
  public static IntegerField MONTH;

  public static DoubleField INITIALLY_PLANNED;
  public static DoubleField ACTUAL;
  public static DoubleField REMAINDER;
  public static DoubleField OVERRUN;

  static {
    GlobTypeLoader.init(BudgetAreaValues.class, "budgetAreaValues");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.write(fieldValues.get(MONTH));
      output.writeDouble(fieldValues.get(INITIALLY_PLANNED));
      output.writeDouble(fieldValues.get(ACTUAL));
      output.writeDouble(fieldValues.get(REMAINDER));
      output.writeDouble(fieldValues.get(OVERRUN));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(data, fieldSetter);
      }
    }

    private void deserializeDataV1(byte[] data, FieldSetter fieldSetter) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(MONTH, input.readNotNullInt());
      fieldSetter.set(INITIALLY_PLANNED, input.readDouble());
      fieldSetter.set(ACTUAL, input.readDouble());
      fieldSetter.set(REMAINDER, input.readDouble());
      fieldSetter.set(OVERRUN, input.readDouble());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
