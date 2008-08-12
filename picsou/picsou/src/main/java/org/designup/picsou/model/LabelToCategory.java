package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class LabelToCategory {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  @Target(Category.class)
  public static LinkField CATEGORY;

  public static IntegerField COUNT;

  public static NotUniqueIndex LABEL_INDEX;

  static {
    GlobTypeLoader.init(LabelToCategory.class, "labelToCategory")
      .defineNotUniqueIndex(LABEL_INDEX, LABEL);
  }

  public static class Serialization implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeString(values.get(LABEL));
      outputStream.writeInteger(values.get(CATEGORY));
      outputStream.writeInteger(values.get(COUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LABEL, input.readString());
      fieldSetter.set(CATEGORY, input.readInteger());
      fieldSetter.set(COUNT, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
