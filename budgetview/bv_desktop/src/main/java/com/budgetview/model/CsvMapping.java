package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class CsvMapping {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;
  
  public static StringField FIELD_NAME;

  public static StringField CSV_TYPE_NAME;

  static {
    TypeLoader.init(CsvMapping.class, "csvMapping");
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(FIELD_NAME));
      outputStream.writeUtf8String(values.get(CSV_TYPE_NAME));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        SerializedInput input = SerializedInputOutputFactory.init(data);
        fieldSetter.set(FIELD_NAME, input.readUtf8String());
        fieldSetter.set(CSV_TYPE_NAME, input.readUtf8String());
      }
    }
  }
}
