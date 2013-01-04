package com.budgetview.shared.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
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

public class SeriesEntity {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(BudgetAreaEntity.class)
  public static LinkField BUDGET_AREA;

  public static StringField NAME;

  static {
    GlobTypeLoader.init(SeriesEntity.class, "seriesEntity");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.write(fieldValues.get(ID));
      output.writeInteger(fieldValues.get(BUDGET_AREA));
      output.writeUtf8String(fieldValues.get(NAME));
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
      fieldSetter.set(BUDGET_AREA, input.readNotNullInt());
      fieldSetter.set(NAME, input.readUtf8String());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
