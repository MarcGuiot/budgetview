package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldSetter;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;

public class SeriesBudget {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Series.class)
  public static LinkField SERIES;

  public static IntegerField MONTH;

  public static DoubleField AMOUNT;

  public static NotUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(SeriesBudget.class);
    loader.defineNonUniqueIndex(SERIES_INDEX, SERIES);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(SeriesBudget.SERIES));
      output.writeInteger(fieldValues.get(SeriesBudget.MONTH));
      output.writeDouble(fieldValues.get(SeriesBudget.AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SeriesBudget.SERIES, input.readInteger());
      fieldSetter.set(SeriesBudget.MONTH, input.readInteger());
      fieldSetter.set(SeriesBudget.AMOUNT, input.readDouble());
    }

    public int getWriteVersion() {
      return 1;
    }
  }
}
