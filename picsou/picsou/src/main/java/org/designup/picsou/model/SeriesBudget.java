package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class SeriesBudget {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Series.class)
  public static LinkField SERIES;

  @Target(Month.class)
  public static LinkField MONTH;

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField OVERRUN_AMOUNT;

  @DefaultInteger(1)
  public static IntegerField DAY;

  public static BooleanField ACTIVE;


  public static MultiFieldUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(SeriesBudget.class, "seriesBudget");
    loader.defineMultiFieldUniqueIndex(SERIES_INDEX, SERIES, MONTH);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(SeriesBudget.SERIES));
      output.writeInteger(fieldValues.get(SeriesBudget.MONTH));
      output.writeDouble(fieldValues.get(SeriesBudget.AMOUNT));
      output.writeInteger(fieldValues.get(SeriesBudget.DAY));
      output.writeBoolean(fieldValues.get(SeriesBudget.ACTIVE));
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
      fieldSetter.set(SeriesBudget.DAY, input.readInteger());
      fieldSetter.set(SeriesBudget.ACTIVE, input.readBoolean());
    }

    public int getWriteVersion() {
      return 1;
    }
  }
}
