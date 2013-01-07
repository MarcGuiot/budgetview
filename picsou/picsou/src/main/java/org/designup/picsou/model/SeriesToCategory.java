package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import com.budgetview.shared.utils.PicsouGlobSerializer;

/** @deprecated */
public class SeriesToCategory {
  public static GlobType TYPE;

  @NoObfuscation
  @Key
  public static IntegerField ID;

  @NoObfuscation
  @Target(Series.class)
  public static LinkField SERIES;

  @NoObfuscation
  @Target(Category.class)
  public static LinkField CATEGORY;

  static {
    GlobTypeLoader.init(SeriesToCategory.class, "seriesToCategory");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(SeriesToCategory.SERIES));
      output.writeInteger(fieldValues.get(SeriesToCategory.CATEGORY));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SeriesToCategory.SERIES, input.readInteger());
      fieldSetter.set(SeriesToCategory.CATEGORY, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }

}
