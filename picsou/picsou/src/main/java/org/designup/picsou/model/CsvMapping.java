package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class CsvMapping {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  
  public static IntegerField IMPORT;

  public static StringField FROM;

  public static StringField TO;

  static {
    GlobTypeLoader.init(CsvMapping.class);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(IMPORT));
      outputStream.writeUtf8String(values.get(FROM));
      outputStream.writeUtf8String(values.get(TO));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        SerializedInput input = SerializedInputOutputFactory.init(data);
        fieldSetter.set(IMPORT, input.readInteger());
        fieldSetter.set(FROM, input.readUtf8String());
        fieldSetter.set(TO, input.readUtf8String());
      }
    }

  }

  ;
}
