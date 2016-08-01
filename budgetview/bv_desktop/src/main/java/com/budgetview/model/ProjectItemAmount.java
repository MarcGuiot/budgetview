package com.budgetview.model;

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
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.FieldValue.value;

public class ProjectItemAmount {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(ProjectItem.class)
  public static LinkField PROJECT_ITEM;

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

  public static Glob findOrCreate(Integer projectItemId, Integer monthId, GlobRepository repository) {
    Glob existing = findUnique(projectItemId, monthId, repository);
    if (existing != null) {
      return existing;
    }
    return repository.create(TYPE,
                             value(PROJECT_ITEM, projectItemId),
                             value(MONTH, monthId));
  }

  public static Glob findUnique(Integer projectItemId, Integer monthId, GlobRepository repository) {
    GlobList all = getAll(projectItemId, monthId, repository);
    if (all.isEmpty()) {
      return null;
    }
    if (all.size() > 1) {
      throw new InvalidState("There should be only one item amount per projectItem and month - actual: " + all);
    }
    return all.getFirst();
  }

  public static GlobList getAll(Integer projectItemId, Integer monthId, GlobRepository repository) {
    return repository
      .findByIndex(PROJECT_ITEM_INDEX, PROJECT_ITEM, projectItemId)
      .findByIndex(MONTH, monthId)
      .getGlobs();
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
