package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.DoublePrecision;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class ProjectItemAmount {
  public static GlobType TYPE;

  @Key
  @Target(Project.class)
  public static LinkField PROJECT_ITEM;

  @Key
  @Target(Month.class)
  public static IntegerField MONTH;

  @DefaultDouble(0.00)
  @DoublePrecision(4)
  public static DoubleField PLANNED_AMOUNT;

  public static MultiFieldUniqueIndex PROJECT_ITEM_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(ProjectItemAmount.class, "projectItemAmount");
    loader.defineMultiFieldUniqueIndex(PROJECT_ITEM_INDEX, PROJECT_ITEM, MONTH);
  }

  public static class Serializer implements PicsouGlobSerializer {
    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(PROJECT_ITEM));
      output.writeInteger(fieldValues.get(MONTH));
      output.writeDouble(fieldValues.get(PLANNED_AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(PROJECT_ITEM, input.readInteger());
      fieldSetter.set(MONTH, input.readInteger());
      fieldSetter.set(PLANNED_AMOUNT, input.readDouble());
    }
  }
}
