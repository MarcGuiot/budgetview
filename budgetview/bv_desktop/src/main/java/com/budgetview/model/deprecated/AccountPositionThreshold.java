package com.budgetview.model.deprecated;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

/** @deprecated */
public class AccountPositionThreshold {
  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @DefaultDouble(0.0)
  public static DoubleField THRESHOLD;

  @DefaultDouble(50.0)
  public static DoubleField THRESHOLD_FOR_WARN;

  static {
    TypeLoader.init(AccountPositionThreshold.class, "accountBalanceLimit");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static Double getValue(GlobRepository repository) {
    Glob limit = repository.find(KEY);
    if (limit == null) {
      return null;
    }
    Double value = limit.get(THRESHOLD);
    if (value == null) {
      return 0.0;
    }
    return value;
  }

  public static class Serializer implements GlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeDouble(values.get(THRESHOLD));
      outputStream.writeDouble(values.get(THRESHOLD_FOR_WARN));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(THRESHOLD, input.readDouble());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(THRESHOLD, input.readDouble());
      fieldSetter.set(THRESHOLD_FOR_WARN, input.readDouble());
    }

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }

}
