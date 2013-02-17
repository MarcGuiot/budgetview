package com.budgetview.shared.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.KeyBuilder;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class BudgetViewVersion {
  public static GlobType TYPE;

  public static org.globsframework.model.Key key;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static IntegerField MAJOR_VERSION;

  @NoObfuscation
  public static IntegerField MINOR_VERSION;

  static {
    GlobTypeLoader.init(BudgetViewVersion.class, "BVVersion");
    key = KeyBuilder.newKey(TYPE, 0);
  }


  public static class Serializer implements PicsouGlobSerializer {

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.write(fieldValues.get(ID));
      output.write(fieldValues.get(MAJOR_VERSION));
      output.write(fieldValues.get(MINOR_VERSION));
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
      fieldSetter.set(MAJOR_VERSION, input.readNotNullInt());
      fieldSetter.set(MINOR_VERSION, input.readNotNullInt());
    }

    public int getWriteVersion() {
      return 1;
    }
  }
}
