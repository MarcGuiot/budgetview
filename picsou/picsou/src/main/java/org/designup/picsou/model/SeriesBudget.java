package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
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
  @Required
  public static LinkField SERIES;

  @Target(Month.class)
  @Required
  public static LinkField MONTH;

  @DefaultDouble(0.0)
  @Required
  public static DoubleField AMOUNT;

  public static DoubleField OBSERVED_AMOUNT;

  @DefaultInteger(1)
  @Required
  public static IntegerField DAY;

  @Required
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
      output.writeDouble(fieldValues.get(SeriesBudget.OBSERVED_AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SeriesBudget.SERIES, input.readInteger());
      fieldSetter.set(SeriesBudget.MONTH, input.readInteger());
      fieldSetter.set(SeriesBudget.AMOUNT, input.readDouble());
      fieldSetter.set(SeriesBudget.DAY, input.readInteger());
      fieldSetter.set(SeriesBudget.ACTIVE, input.readBoolean());
      fieldSetter.set(SeriesBudget.OBSERVED_AMOUNT, null);
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SeriesBudget.SERIES, input.readInteger());
      fieldSetter.set(SeriesBudget.MONTH, input.readInteger());
      fieldSetter.set(SeriesBudget.AMOUNT, input.readDouble());
      fieldSetter.set(SeriesBudget.DAY, input.readInteger());
      fieldSetter.set(SeriesBudget.ACTIVE, input.readBoolean());
      fieldSetter.set(SeriesBudget.OBSERVED_AMOUNT, input.readDouble());
    }


    public int getWriteVersion() {
      return 2;
    }
  }
}
