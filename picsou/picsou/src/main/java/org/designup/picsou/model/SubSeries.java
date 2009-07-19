package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldSetter;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;

public class SubSeries {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Series.class)
  public static LinkField SERIES;

  @NamingField
  public static StringField NAME;

  static {
    GlobTypeLoader.init(SubSeries.class, "subSeries");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(SubSeries.SERIES));
      output.writeUtf8String(fieldValues.get(SubSeries.NAME));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SubSeries.SERIES, input.readInteger());
      fieldSetter.set(SubSeries.NAME, input.readUtf8String());
    }
  }
}